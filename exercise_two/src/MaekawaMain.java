import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

public class MaekawaMain {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
        Registry registry = LocateRegistry.createRegistry(1099);
        int TOTAL_THREADS = 4;
        int TOTAL_RESOURCES = 2;
        MaekawaComponent[] objList = new MaekawaComponent[TOTAL_THREADS];
        CyclicBarrier myBarrier = new CyclicBarrier(TOTAL_THREADS, new Runnable() {
            @Override
            public void run() {
                System.out.println("All processes done.");
                System.exit(0);
            }
        });

        for (int i = 0; i < TOTAL_THREADS; i++) {
            MaekawaComponent obj = new MaekawaComponent();
            obj.setId(i+1);
            obj.setName(i+1);
            obj.initResources(TOTAL_RESOURCES);
            obj.buildRequestSet(TOTAL_THREADS);
            registry.bind("process-" + (i+1), obj);
            objList[i] = (MaekawaComponent) registry.lookup("process-" + (i+1));
        }

        HashMap<Integer, int[]> requests = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader("exercise_two/src/requests.txt"));
            Integer i = 1;
            String str;
            while ((str = in.readLine()) != null) {
                requests.put(i, Arrays.stream(str.split(" ")).mapToInt(Integer::parseInt).toArray());
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyThread t = new MyThread(myBarrier, TOTAL_THREADS, requests, objList);
        Thread[] threads = new Thread[TOTAL_THREADS];
        for (int i = 0; i < registry.list().length; i++) {
            Integer pid = i+1;
            objList[i].setComponents(objList);
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
    private HashMap<Integer, int[]> requests;
    private MaekawaComponent obj;
    private MaekawaComponent[] objList;
    private int totalThreads;

    MyThread(CyclicBarrier barrier, int totalThreads, HashMap<Integer, int[]> requests, MaekawaComponent[] objList) {
        this.myBarrier = barrier;
        this.totalThreads = totalThreads;
        this.requests = requests;
        this.objList = objList;
    }

    @Override
    public void run() {
        try {
            this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads + 1;
            for (int i : requests.get(myThreadIndex)) {
                synchronized (this) {
                    this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads + 1;
                    this.obj = objList[myThreadIndex-1];
                    if (i != 0)
                        obj.request(i);
                }
                Thread.sleep(new Random().nextInt(1000));
            }
            System.out.println("Thread " + myThreadIndex + " Done Now:" + System.currentTimeMillis());  // TODO: wrong thread id
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


// TODO: deadlock detection (with scalar clock?)
// TODO: randomness between access
// TODO: other TODOs in Component