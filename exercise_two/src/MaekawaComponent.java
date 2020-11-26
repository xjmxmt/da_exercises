import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.PriorityBlockingQueue;

public class MaekawaComponent extends UnicastRemoteObject implements MaekawaInterfaceRMI {
    private static final long serialVersionUID = 7526471155622776147L;
    private int id;
    private String name;
//    private HashSet<Integer> requestSet = new HashSet<>();  // the
    private MaekawaInterfaceRMI[] requestSet;
    private HashMap<Integer, Boolean> states = new HashMap<>();  // shows resources are currently occupied or not
    final private HashMap<Integer, Queue<Integer>> waitlist = new HashMap<>();  // shows processes waiting for certain resources
    private HashMap<Integer, Integer> waitAck = new HashMap<>();
    private MaekawaComponent[] objList;
    public Queue<Request> receivedRqst = new PriorityBlockingQueue<>(100, new Comparator<Request>() {
        @Override
        public int compare(Request r1, Request r2) {
            if (r1.getScalarClock().isSmallerThan(r2.getScalarClock())) return -1;
            else return 1;
        }
    });
    private Message msg;
    private AtomicBoolean granted = new AtomicBoolean(false);
    public AtomicInteger no_grants = new AtomicInteger();
    private AtomicInteger timer;
    public AtomicReference<Request> current_grant = new AtomicReference<>();
    private AtomicBoolean inquiring = new AtomicBoolean(false);
    private AtomicBoolean postponed = new AtomicBoolean(false);

    public MaekawaComponent() throws RemoteException {

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
    public void setTimer(AtomicInteger timer) throws RemoteException {
        this.timer = timer;
    }

    @Override
    public void setComponents(MaekawaInterfaceRMI[] l) throws RemoteException {
        this.requestSet = l;
    }

    public void setMsg(Message msg) throws RemoteException {
        this.msg = msg;
    }

    public void setObjList(MaekawaComponent[] objList) throws RemoteException {
        this.objList = objList;
    }

    @Override
    public void initResources(int n) throws RemoteException {
        for (int i=1; i <= n; i++) {
            states.put(i, Boolean.FALSE);
            waitlist.put(i, new LinkedList<Integer>());
        }
    }

    @Override
    public void buildRequestSet(int total) throws RemoteException {
//        for (int i=1; i <= total; i++){
//            this.requestSet.add(i);
//        }
    }

    @Override
    public void request() throws RemoteException {
        no_grants.set(0);
        for (MaekawaInterfaceRMI component : requestSet) {
            timer.incrementAndGet();  // increase at every step
//            new Thread ( () -> {
//                try {
//                    Thread.sleep((int)(Math.random()*5));  // delay
//                    component.receiveRequest(new ScalarClock(timer.intValue(), this.id), this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
            try {
                Thread.sleep((int)(Math.random()*5));  // delay
                component.receiveRequest(new ScalarClock(timer.intValue(), this.id), this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    public void sendMessage(String type, MaekawaComponent receiver) throws RemoteException {
//        if (type == "request") {
//            new Thread(() -> {
//                receiver.receiveRequest();
//            })
//        }
//    }

    @Override
    public void receiveRequest(ScalarClock scalarClock, MaekawaInterfaceRMI sender) throws RemoteException {
        timer.incrementAndGet();
        if (!granted.get()) {
            current_grant.set(new Request(scalarClock, sender));
//            receivedRqst.add(current_grant.get());
            granted.set(true);
            sender.receiveGrant();
        } else {
            receivedRqst.add(new Request(scalarClock, sender));
            Request head = receivedRqst.peek();
            if (current_grant.get().getScalarClock().isSmallerThan(scalarClock) || head.getScalarClock().isSmallerThan(scalarClock)) {
                sender.receivePostpone();
            } else if (!inquiring.get()) {
                inquiring.set(true);
                sender.receiveInquire(this);
            }
        }
    }

    @Override
    public void receiveGrant() throws RemoteException {
        timer.incrementAndGet();
        no_grants.incrementAndGet();
        if (no_grants.intValue() == requestSet.length) {
            postponed.set(false);
            System.out.println(getName() + " enter critical region.");
            doSomeOperations();
            System.out.println(getName() + " release critical region.");
            for (MaekawaInterfaceRMI component : requestSet) {
                component.release();
            }
        }
    }

    @Override
    public void doSomeOperations() throws RemoteException {
        //System.out.println(getName() + " enter critical region.");
        msg.incrementContent();
        msg.printMsg(new ScalarClock(timer.intValue(), id));
    }

    @Override
    public void release() throws RemoteException {
        timer.incrementAndGet();
        granted.set(false);
        inquiring.set(false);
//        receivedRqst.remove(current_grant.get());
//        current_grant.set(null);
        if (receivedRqst.size() != 0) {
            current_grant.set(receivedRqst.poll());
            MaekawaInterfaceRMI receiver = current_grant.get().getSender();
            granted.set(true);
            receiver.receiveGrant();
        }
    }

    @Override
    public void receivePostpone() throws RemoteException {
        timer.incrementAndGet();
        postponed.set(true);
    }

    @Override
    public void receiveInquire(MaekawaInterfaceRMI sender) throws RemoteException {
        timer.incrementAndGet();
        while (!postponed.get() && no_grants.intValue() != requestSet.length) {
            try {
                Thread.sleep(5);  // waiting
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (postponed.get()) {
            no_grants.decrementAndGet();
            sender.receiveRelinquish();
        }
    }

    @Override
    public void receiveRelinquish() throws RemoteException {
        inquiring.set(false);
        granted.set(false);
        receivedRqst.add(current_grant.get());
        current_grant.set(receivedRqst.poll());
        MaekawaInterfaceRMI receiver = current_grant.get().getSender();
        granted.set(true);
        receiver.receiveGrant();
    }

//    @Override
//    public void receiveRelease(int i) throws RemoteException {
//        synchronized (waitlist) {
//            if (!waitlist.get(i).isEmpty()) {
//                int next = waitlist.get(i).poll();
//                new Thread(() -> {
//                    try {
//                        components[next - 1].receiveAck(id, i);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//                ).start();
//            }
//            else {
//                states.put(i, Boolean.FALSE);
//            }
//        }
//    }
}
