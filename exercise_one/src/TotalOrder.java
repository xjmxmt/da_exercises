import java.rmi.RemoteException;
import java.util.*;

public class TotalOrder implements TotalOrderInterfaceRMI {
    private int id;
    private String name;
    private int time = 0;
    private int timeUnit = 1;
    private TotalOrder[] processList;
    private PriorityQueue<Message> receivedMsg = new PriorityQueue<Message>(new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            if (o1.scalarClock.isSmallerThan(o2.scalarClock)) return -1;
            else return 1;
        }
    });
    private HashMap<ScalarClock, Integer> ackMap = new HashMap<>();
    private LinkedList<Message> deliveredMsg = new LinkedList<>();

    public TotalOrder() {

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
    public void setProcessList(TotalOrder[] registryList) throws RemoteException{
        this.processList = registryList;
    }

    @Override
    public TotalOrder[] getProcessList() {
        return processList;
    }

    @Override
    public void setAck(ScalarClock sc) throws RemoteException {
        if (ackMap.get(sc) == null) ackMap.put(sc, 1);
        else ackMap.put(sc, ackMap.get(sc) + 1);
    }

    public HashMap<ScalarClock, Integer> getAckMap() throws RemoteException{
        return ackMap;
    }

    @Override
    public Queue<Message> getReceivedMsg() throws RemoteException {
        return this.receivedMsg;
    }

    @Override
    public String getDeliveredMsg() throws RemoteException {
        return this.getName() + " delivered: " + this.deliveredMsg;
    }

    @Override
    public Message broadcast(String s) throws RemoteException {
        this.time += this.timeUnit;
        Message msg = new Message();
        msg.setMessage(s, new ScalarClock(time, id));

        for (TotalOrder obj : processList) {
            new Thread(() -> {
                try {
                    obj.receive(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        for (TotalOrder obj : processList) {
            new Thread(() -> {
                try {
                    while (obj.getReceivedMsg().size() != 0
                            && obj.getAckMap().get(obj.getReceivedMsg().peek().scalarClock) == 3) {
                        obj.deliver(obj.getReceivedMsg().poll());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        return msg;
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        this.receivedMsg.add(msg);

        for (TotalOrder p : processList) {
            p.setAck(msg.scalarClock);
        }
    }

    @Override
    public void deliver(Message msg) throws RemoteException {
        this.deliveredMsg.add(msg);
    }
}

