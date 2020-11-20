import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TotalOrder implements TotalOrderInterfaceRMI {

    private int id;
    private String name;
    private long time = 0;
    private int timeUnit = 1;
    private int threadNum;
    private TotalOrder[] processList = new TotalOrder[threadNum];
    private PriorityQueue<Message> receivedMsg = new PriorityQueue<Message>(100, new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            if (o1.scalarClock.isSmallerThan(o2.scalarClock)) return -1;
            else return 1;
        }
    });
    private TreeMap<ScalarClock, Integer> ackMap = new TreeMap<>();
    private LinkedList<Message> receivedOrderedMsg = new LinkedList<>();
    private LinkedList<Message> deliveredMsg = new LinkedList<>();
    private PriorityQueue<Message> waitList = new PriorityQueue<Message>(100, new Comparator<Message>() {
        @Override
        public int compare(Message o1, Message o2) {
            if (o1.scalarClock.isSmallerThan(o2.scalarClock)) return -1;
            else return 1;
        }
    });
    private Message lastMsg = null;
    private long beginTime = time*threadNum+1;

    private ReentrantLock lock = new ReentrantLock();

    public String getWaitList() throws RemoteException {
        return " wait list: " + this.waitList;
    }

    public TotalOrder(int threadNum) throws RemoteException, NotBoundException {
        this.threadNum = threadNum;
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
    public void setAck(Message msg) throws RemoteException {
        ScalarClock sc = msg.scalarClock;
        if (ackMap.get(sc) == null) ackMap.put(sc, 1);
        else {
            ackMap.put(sc, ackMap.get(sc) + 1);
            while (getReceivedMsg().size() != 0) {
                Message front = getReceivedMsg().peek();
                assert front != null;
                if (ackMap.get(front.scalarClock) == null) break;
                if (ackMap.get(front.scalarClock) < threadNum - 1) break;
                if (ackMap.get(front.scalarClock) == threadNum - 1) {
                    boolean flag = false;
                    Iterator it = ackMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry) it.next();
                        ScalarClock key = (ScalarClock) entry.getKey();
                        if (key.compareTo(front.scalarClock) == 1) break;
                        if (key.compareTo(front.scalarClock) == -1) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                    ackMap.remove(front.scalarClock);
                    deliver(getReceivedMsg().poll());
                }
            }
        }
    }

    public void clearAck() throws RemoteException {
        while (getReceivedMsg().size() != 0) {
            Message front = getReceivedMsg().peek();
            assert front != null;
            if (ackMap.get(front.scalarClock) == null) break;
            if (ackMap.get(front.scalarClock) < threadNum - 1) break;
            if (ackMap.get(front.scalarClock) == threadNum - 1) {
                deliver(getReceivedMsg().poll());
            }
        }
    }

    public TreeMap<ScalarClock, Integer> getAckMap() throws RemoteException{
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
        Message msg = new Message();
        msg.setMessage(s, new ScalarClock(time*threadNum+id, id));
        this.time += this.timeUnit;

        // send message with RMI TotalOrder.receive()
        for (TotalOrderInterfaceRMI obj : processList) {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                obj.checkWaitList(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return msg;
    }

    @Override
    public void checkWaitList(Message msg) throws RemoteException {
        if (msg.scalarClock.time == beginTime) {
            if (lastMsg == null) lastMsg = msg;
            else {
                waitList.add(lastMsg);
                lastMsg = msg;
            }
            receive(msg);
        } else {
            if (lastMsg == null) lastMsg = msg;
            else if (msg.scalarClock.time - lastMsg.scalarClock.time == timeUnit) {
                lastMsg = msg;
                receive(msg);
            } else if (msg.scalarClock.time - lastMsg.scalarClock.time != timeUnit) {
                waitList.add(msg);
                while (waitList.size() != 0) {
                    Message nextMsg = null;
                    Message front = waitList.peek();
                    assert front != null;
                    if (front.scalarClock.time - lastMsg.scalarClock.time == timeUnit) nextMsg = waitList.poll();
                    if (nextMsg != null) {
                        lastMsg = nextMsg;
                        receive(nextMsg);
                    } else break;
                }
            }
        }
    }

    public void clearWaitList() throws RemoteException {
        while (waitList.size() != 0) {
            Message nextMsg = null;
            Message front = waitList.peek();
            assert front != null;
            if (front.scalarClock.time - lastMsg.scalarClock.time == timeUnit) nextMsg = waitList.poll();
            if (nextMsg != null) {
                lastMsg = nextMsg;
                receive(nextMsg);
            } else break;
        }
    }

    @Override
    public void receive(Message msg) throws RemoteException {
        this.receivedMsg.add(msg);
        this.receivedOrderedMsg.add(msg);
        for (TotalOrder p : processList) {
            if (p != processList[id - 1]) {
                try {
                    Thread.sleep(new Random().nextInt(100));
                    p.setAck(msg);
                } catch (RemoteException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deliver(Message msg) throws RemoteException {
        System.out.println(this.getName() + " " + msg + " has been delivered.");
        this.deliveredMsg.add(msg);
    }
}

