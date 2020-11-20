import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

public class TotalOrderMain {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
        Registry registry = LocateRegistry.createRegistry(1099);
        int TOTAL_THREADS = 4;
        TotalOrder[] objList = new TotalOrder[TOTAL_THREADS];
        CyclicBarrier myBarrier = new CyclicBarrier(TOTAL_THREADS, new Runnable() {
            @Override
            public void run() {
                for (TotalOrder obj : objList) {
                    try {
                        obj.clearWaitList();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                for (TotalOrder obj : objList) {
                    try {
                        obj.clearAck();
//                        System.out.println(obj.getName() + " " + obj.getReceivedMsg());
//                        System.out.println(obj.getName() + " " + obj.getWaitList());
//                        System.out.println(obj.getName() + " " + obj.getReceivedOrderedMsg());
                        System.out.println(obj.getName() + " " + obj.getDeliveredMsg());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("All processes done.");
                System.exit(0);
            }
        });

        for (int i = 0; i < TOTAL_THREADS; i++) {
            TotalOrder obj = new TotalOrder(TOTAL_THREADS);
            obj.setId(i+1);
            obj.setName(i+1);
            registry.bind("process-" + (i+1), obj);
            objList[i] = (TotalOrder) registry.lookup("process-" + (i+1));
        }

        HashMap<Integer, String[]> msgToSend = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("src/msgToSend.txt"));
            Integer i = 1;
            String str;
            while ((str = in.readLine()) != null) {
                msgToSend.put(i, str.split(" "));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyThread t = new MyThread(myBarrier, TOTAL_THREADS, msgToSend, objList);
        Thread[] threads = new Thread[TOTAL_THREADS];
        for (int i = 0; i < registry.list().length; i++) {
            TotalOrder obj = objList[i];
            obj.setProcessList(objList);
            Integer pid = i+1;
            Thread thread = new Thread(t);
            threads[i] = thread;
        }

        for (int i = 0; i < TOTAL_THREADS; i++) {
            threads[i].start();
        }
    }
}

class MyThread implements Runnable {

    private CyclicBarrier myBarrier;
    private int myThreadIndex;
    private HashMap<Integer, String[]> strings;
    private TotalOrder obj;
    private TotalOrder[] objList;
    private int totalThreads;

    MyThread(CyclicBarrier barrier, int totalThreads, HashMap<Integer, String[]> strings, TotalOrder[] objList) {
        this.myBarrier = barrier;
        this.totalThreads = totalThreads;
        this.strings = strings;
        this.objList = objList;
    }

    @Override
    public void run() {
        try {
            this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads + 1;
            for (String string : strings.get(myThreadIndex)) {
                synchronized (this) {
                    this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads + 1;
                    this.obj = objList[myThreadIndex-1];
                    System.out.println(obj.getName() + " get the lock");
                    obj.broadcast(string);
                }
                Thread.sleep(new Random().nextInt(50));
            }
            System.out.println("Thread " + myThreadIndex + " Done Now:" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            myBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
