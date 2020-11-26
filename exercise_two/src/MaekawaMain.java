import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class MaekawaMain {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {
        System.out.println(args);
        int TOTAL_THREADS = Integer.parseInt(args[0]);
        String rsPath = args[1];
        int pid = Integer.parseInt(args[2]);
        int SET_LENGTH = (int) Math.ceil(Math.sqrt(TOTAL_THREADS));
        Registry registry = LocateRegistry.getRegistry(1099);
        MaekawaInterfaceRMI[] objList = new MaekawaInterfaceRMI[TOTAL_THREADS];
        // lookup object list
        for (int i = 0; i < TOTAL_THREADS; i++) {
            objList[i] = (MaekawaInterfaceRMI) registry.lookup("process-" + (i));
        }

        // read request sets
        HashMap<Integer, int[]> requestSets = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(rsPath));
            Integer i = 0;
            String str;
            while ((str = in.readLine()) != null) {
                requestSets.put(i, Arrays.stream(str.split(" ")).mapToInt(Integer::parseInt).toArray());
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyThread t = new MyThread(null, TOTAL_THREADS, requestSets, objList, pid);
        Thread thread = new Thread(t);
        MaekawaInterfaceRMI[] l = new MaekawaInterfaceRMI[SET_LENGTH];
        int i = 0;
        for (int j : requestSets.get(pid)) {
            l[i] = objList[j];
            i++;
        }
        objList[pid].setComponents(l);
        thread.start();
    }
}

class MyThread implements Runnable {

    private CyclicBarrier myBarrier;
    private int myThreadIndex;
    private HashMap<Integer, int[]> requestSets;
    private MaekawaInterfaceRMI obj;
    private MaekawaInterfaceRMI[] objList;
    private int totalThreads;

    MyThread(CyclicBarrier barrier, int totalThreads, HashMap<Integer, int[]> requestSets, MaekawaInterfaceRMI[] objList, int threadId) {
        this.myBarrier = barrier;
        this.totalThreads = totalThreads;
        this.requestSets = requestSets;
        this.objList = objList;
        this.myThreadIndex = threadId;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000 * totalThreads);  // sleep until every thread is ready to start
            //this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads;
            System.out.println("initial Thread: " + myThreadIndex);
            //int requestTime = new Random().nextInt(5) + 1;
            int requestTime = 5;
            for (int i=0; i < requestTime; i++) {
                Thread.sleep((new Random().nextInt(100)+100)*totalThreads);  // delay
                //this.myThreadIndex = ((int) Thread.currentThread().getId()) % totalThreads;
                this.obj = objList[myThreadIndex];
                obj.request();
            }
            Thread.sleep((new Random().nextInt(100)+100)*totalThreads);
            System.out.println("Thread " + myThreadIndex + " Done Now:" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            myBarrier.await();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}