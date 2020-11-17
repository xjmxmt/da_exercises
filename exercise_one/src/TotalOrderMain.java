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
import java.util.concurrent.locks.ReentrantLock;

public class TotalOrderMain {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
        Registry registry = LocateRegistry.createRegistry(1099);
        final int TOTAL_THREADS = 4;
        ExecutorService myExecutor = Executors.newFixedThreadPool(TOTAL_THREADS);
        TotalOrder[] objList = new TotalOrder[TOTAL_THREADS];
        final CyclicBarrier myBarrier = new CyclicBarrier(TOTAL_THREADS, new Runnable() {
            @Override
            public void run() {

                for (TotalOrder obj : objList) {
                    try {
                        System.out.println(obj.getName() + " " + obj.getReceivedMsg());
                        System.out.println(obj.getName() + " " + obj.getReceivedOrderedMsg());
                        System.out.println(obj.getName() + " " + obj.getDeliveredMsg());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        for (int i = 0; i < TOTAL_THREADS; i++) {
            TotalOrder obj = new TotalOrder();
            obj.setId(i+1);
            obj.setName(i+1);
            registry.bind("process " + (i+1), obj);
            objList[i] = obj;
        }

        HashMap<Integer, String[]> msgToSend = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("exercise_one/src/msgToSend.txt"));
            Integer i = 1;
            String str;
            while ((str = in.readLine()) != null) {
                msgToSend.put(i, str.split(" "));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < registry.list().length; i++) {
            TotalOrder obj = objList[i];
            obj.setProcessList(objList);
            Integer pid = i+1;
            myExecutor.execute(new MyThread(myBarrier, pid, msgToSend.get(pid), objList));
        }
    }
}

class MyThread implements Runnable {

    private final CyclicBarrier myBarrier;
    private final int myThreadIndex;
    private String[] strings;
    private TotalOrder obj;
    private TotalOrder[] objList;
    private ReentrantLock lock = new ReentrantLock();

    private int strindex = 0;

    MyThread(final CyclicBarrier barrier, final int threadIndex, String[] strings, TotalOrder[] objList) {
        this.myBarrier = barrier;
        this.myThreadIndex = threadIndex;
        this.strings = strings;
        this.objList = objList;
        this.obj = objList[myThreadIndex-1];
    }

    public int getStrindex() {
        return strindex;
    }

    @Override
    public void run() {
        System.out.println("Process " + myThreadIndex + " is running...");
        try {
            for (String string : strings) {
                // broadcast the message
                Message msg = obj.broadcast(string);
                Thread.sleep(300);
            }
            Thread.sleep(1000);
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
