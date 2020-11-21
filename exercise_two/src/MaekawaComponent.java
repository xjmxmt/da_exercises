import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.IntStream;

public class MaekawaComponent implements MaekawaInterfaceRMI {
    private int id;
    private String name;
    private HashSet<Integer> requestSet = new HashSet<>();  // the
    private MaekawaComponent[] components;
    private HashMap<Integer, Boolean> states = new HashMap<>();  // shows resources are currently occupied or not
    final private HashMap<Integer, Queue<Integer>> waitlist = new HashMap<>();  // shows processes waiting for certain resources
    private HashMap<Integer, Integer> waitAck = new HashMap<>();

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
    public void setComponents(MaekawaComponent[] l) throws RemoteException {
        this.components = l;
    }

    @Override
    public void initResources(int n) throws RemoteException {
        for (int i=1; i <= n; i++) {
            states.put(i, Boolean.FALSE);
            waitlist.put(i, new LinkedList<Integer>());
        }
    }

    @Override
    public void buildRequestSet(int total) throws RemoteException {  // TODO: build real request sets
        for (int i=1; i <= total; i++){
            this.requestSet.add(i);
        }
    }

    @Override
    public void request(int i) throws RemoteException {
        waitAck.put(i, requestSet.size());
        for (int c: requestSet) {
            new Thread(() -> {
                try {
                    components[c - 1].receiveRequest(id, i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            ).start();
        }
    }

    @Override
    public void receiveRequest(int sender, int i) throws RemoteException {
        System.out.println("Process " + sender + " requests " + i + " from Process " + id);
        if (!states.get(i)) {
            states.put(i, Boolean.TRUE);
            new Thread(() -> {
                try {
                    components[sender - 1].receiveAck(id, i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            ).start();

        }
        else
        {
            synchronized (waitlist) {
                waitlist.get(i).add(sender);
            }
        }

    }

    @Override
    public synchronized void receiveAck(int sender, int i) throws RemoteException {   // TODO: is synchronized needed here?
        System.out.println("Process " + sender + " ACK " + i + " to Process " + id);
        int n = waitAck.get(i);
        if (n == 1) {
            waitAck.remove(i);
            System.out.println("Process " + id + " has " + i);
            new Thread(() -> {
                try {
                    Thread.sleep(new Random().nextInt(100));  // hold resource for some time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    System.out.println("Process " + id + " releases " + i);
                    this.release(i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            ).start();
        }
        else {
            waitAck.put(i, n - 1);
        }
    }

    @Override
    public void release(int i) throws RemoteException {
        waitAck.put(i, requestSet.size());
        for (int c: requestSet) {
            new Thread(() -> {
                try {
                    components[c - 1].receiveRelease(i);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            ).start();
        }
    }

    @Override
    public void receiveRelease(int i) throws RemoteException {
        synchronized (waitlist) {
            if (!waitlist.get(i).isEmpty()) {
                int next = waitlist.get(i).poll();
                new Thread(() -> {
                    try {
                        components[next - 1].receiveAck(id, i);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                ).start();
            }
            else {
                states.put(i, Boolean.FALSE);
            }
        }
    }
}
