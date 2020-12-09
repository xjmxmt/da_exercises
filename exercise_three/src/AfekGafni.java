import javax.swing.*;
import java.rmi.RemoteException;
import java.util.*;

public class AfekGafni implements AfekGafniInterfaceRMI, java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private int level = 0;
    private int pos = 0;
    private Message parent = null;
    private Message potential = null;
    private boolean candidate = true;
    private AfekGafniInterfaceRMI[] objList;
    private List<Integer> untraversed = new ArrayList<Integer>();

    @Override
    public void setId(int i) throws RemoteException {
        this.id = i;
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    @Override
    public void setName(int i) throws RemoteException {
        this.name = "process " + i;
    }

    @Override
    public String getName() throws RemoteException{
        return name;
    }

    @Override
    public void setCandidate(boolean b) throws RemoteException {
        this.candidate = b;
    }

    @Override
    public void setObjList(AfekGafniInterfaceRMI[] objList) throws RemoteException {
        this.objList = objList;
    }

    @Override
    public void wakeup() throws RemoteException {
        assert this.candidate;
        for (int i = 0; i < objList.length; i++)
            if (i + 1 != id)
                untraversed.add(i + 1);
        Collections.shuffle(untraversed);
        System.out.println("Process " + id + untraversed);
        send();
    }

    @Override
    public synchronized void send() throws RemoteException {
        if (level == untraversed.size()) {
            System.out.println("Process " + id + " ELECTED");
            return;
        }
        if (!candidate || pos > level)
            return;

        int i = untraversed.get(pos);
        System.out.println(id + " send to " + untraversed.get(pos));
        pos++;
        try {
            Thread.sleep((int) (Math.random() * 100));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        objList[i - 1].receive(new Message(level, id, id));
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        System.out.println(id + " receive " + msg + " from " + msg.sender);
        if (candidate) {
            if (id == msg.id) {
                synchronized (this) {
                    level++;
                }
                new Thread ( () -> {
                    try {
                        send();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            else if (msg.compareTo(new Message(level, id, id)) > 0) {
                System.out.println("Process " + id + " RESIGNED");
                candidate = false;
                parent = msg;
                new Thread ( () -> {
                    try {
                        objList[msg.sender - 1].receive(new Message(msg.level, msg.id, id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        else {
            if (potential == null || msg.compareTo(potential) > 0) {
                potential = msg;
                if (parent == null) {
                    System.out.println("Process " + id + " set " + msg.id + " as parent and ack");
                    parent = msg;
                    new Thread ( () -> {
                        try {
                            objList[parent.id - 1].receive(new Message(potential.level, potential.id, id));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                }
                else {
                    System.out.println("Process " + id + " attempt to kill " + parent.id);
                    new Thread ( () -> {
                        try {
                            objList[parent.id - 1].receive(new Message(potential.level, potential.id, id));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

            } else if (msg.compareTo(potential) == 0) {
                parent = potential;
                System.out.println("Process " + id + " set " + msg.id + " as parent and ack");
                new Thread ( () -> {
                    try {
                        objList[parent.id - 1].receive(new Message(potential.level, potential.id, id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        }
    }
}
