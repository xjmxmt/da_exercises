import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.rmi.server.UnicastRemoteObject;

public class AfekGafni extends UnicastRemoteObject implements AfekGafniInterfaceRMI {

    private static final long serialVersionUID = 7526471155622776147L;
    private int id;
    private String name;
    private int level = 0;
    private int pos = 0;
    private AtomicReference<Message> parent = new AtomicReference<>();
    private AtomicReference<Message> potential = new AtomicReference<>();
    private AtomicBoolean candidate = new AtomicBoolean(false);
    private AfekGafniInterfaceRMI[] objList;
    private List<Integer> untraversed = new ArrayList<Integer>();

    public AfekGafni() throws RemoteException {

    }

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
        this.candidate.set(b);
    }

    @Override
    public void setObjList(AfekGafniInterfaceRMI[] objList) throws RemoteException {
        this.objList = objList;
    }

    @Override
    public void wakeup() throws RemoteException {
        assert this.candidate.get();
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
        if (!candidate.get() || pos > level)
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
        if (candidate.get()) {
            if (id == msg.id) {
                synchronized (this) {
                    level++;
                }
//                new Thread ( () -> {
//                    try {
//                        send();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).start();
                send();
            }
            else if (msg.compareTo(new Message(level, id, id)) > 0) {
                System.out.println("Process " + id + " RESIGNED");
                candidate.set(false);
                parent.set(msg);
//                new Thread ( () -> {
//                    try {
//                        objList[msg.sender - 1].receive(new Message(msg.level, msg.id, id));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).start();
                objList[msg.sender - 1].receive(new Message(msg.level, msg.id, id));
            }
        }
        else {
            if (potential.get() == null || msg.compareTo(potential.get()) > 0) {
                potential.set(msg);
                if (parent.get() == null) {
                    System.out.println("Process " + id + " set " + msg.id + " as parent and ack");
                    parent.set(msg);
//                    new Thread ( () -> {
//                        try {
//                            objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }).start();
                    objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
                }
                else {
                    System.out.println("Process " + id + " attempt to kill " + parent.get().id);
//                    new Thread ( () -> {
//                        try {
//                            objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }).start();
                    objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
                }

            } else if (msg.compareTo(potential.get()) == 0) {
                parent.set(potential.get());
                System.out.println("Process " + id + " set " + msg.id + " as parent and ack");
//                new Thread ( () -> {
//                    try {
//                        objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }).start();
                objList[parent.get().id - 1].receive(new Message(potential.get().level, potential.get().id, id));
            }
        }
    }
}
