import java.rmi.RemoteException;
import java.util.*;

public class TotalOrder implements TotalOrderInterfaceRMI {
    private int id;
    private String name;
    private int time = 0;
    private int timeUnit = 1;
    private TotalOrder[] processList;
    private int threadNum;
    private PriorityQueue<Message> receivedMsg = new PriorityQueue<Message>(new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            if (o1.scalarClock.isSmallerThan(o2.scalarClock)) return -1;
            else return 1;
        }
    });
    private HashMap<ScalarClock, Integer> ackMap = new HashMap<>();
    private LinkedList<Message> receivedOrderedMsg = new LinkedList<>();
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
        this.threadNum = processList.length;
    }

    @Override
    public TotalOrder[] getProcessList() throws RemoteException{
        return processList;
    }

    @Override
    public void setAck(ScalarClock sc) throws RemoteException {
        if (ackMap.get(sc) == null) ackMap.put(sc, 1);
        else {
            ackMap.put(sc, ackMap.get(sc) + 1);
            while (getReceivedMsg().size() != 0) {
                Message front = getReceivedMsg().peek();
                //System.out.println(id + " " + front);
                assert front != null;
                if (ackMap.get(front.scalarClock) == null) break;
                if (ackMap.get(front.scalarClock) < threadNum - 1) break;
                if (ackMap.get(front.scalarClock) == threadNum - 1) {
                    deliver(getReceivedMsg().poll());
                }
            }
        }

    }

    public HashMap<ScalarClock, Integer> getAckMap() throws RemoteException{
        return ackMap;
    }

    @Override
    public Queue<Message> getReceivedMsg() throws RemoteException {
        return this.receivedMsg;
    }

    @Override
    public String getReceivedOrderedMsg() throws RemoteException {
        return " received: " + this.receivedOrderedMsg;
    }

    @Override
    public String getDeliveredMsg() throws RemoteException {
        return " delivered: " + this.deliveredMsg;
    }

    @Override
    public Message broadcast(String s) throws RemoteException {
        this.time += this.timeUnit;
        Message msg = new Message();
        msg.setMessage(s, new ScalarClock(time, id));

        // send message with RMI TotalOrder.receive()
        this.receive(msg);
        for (TotalOrder obj : processList)
        if (obj != processList[id - 1])
        {
            new Thread(() -> {
                try {
                    Thread.sleep(new Random().nextInt(100));
                    obj.receive(msg);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        return msg;
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        this.receivedMsg.add(msg);
        this.receivedOrderedMsg.add(msg);
        for (TotalOrder p : processList)
            if (p != processList[id - 1])
            {
                new Thread(() -> {
                    try {
                        Thread.sleep(new Random().nextInt(100));
                        p.setAck(msg.scalarClock);
                    } catch (RemoteException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            }
    }

    @Override
    public void deliver(Message msg) throws RemoteException {
        this.deliveredMsg.add(msg);
    }
}

