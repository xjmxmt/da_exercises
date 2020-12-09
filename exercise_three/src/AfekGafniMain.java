import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;

public class AfekGafniMain {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, NotBoundException {

        Registry registry = LocateRegistry.createRegistry(1099);

        int TOTAL_THREADS = 10;
        String rsPath = "exercise_three/src/test.txt";
        int SET_LENGTH = (int) Math.ceil(Math.sqrt(TOTAL_THREADS));

        AfekGafniInterfaceRMI[] objList = new AfekGafniInterfaceRMI[TOTAL_THREADS];
        // lookup object list
        for (int i = 0; i < TOTAL_THREADS; i++) {

            AfekGafni obj = new AfekGafni();
            obj.setId(i+1);
            obj.setName(i+1);
            obj.setObjList(objList);
            registry.bind("process-" + (i+1), obj);

            objList[i] = (AfekGafniInterfaceRMI) registry.lookup("process-" + (i+1));
        }

        // read request sets
        HashMap<Integer, Integer> requestSets = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(rsPath));
            int i = 0;
            String str;
            while ((str = in.readLine()) != null) {
                requestSets.put(i, Integer.parseInt(str));
                i += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int pid = 0; pid < TOTAL_THREADS; pid++) {
            MyThread t = new MyThread(null, TOTAL_THREADS, requestSets.get(pid), objList, pid);
            Thread thread = new Thread(t);
            thread.start();
        }
    }
}

class MyThread implements Runnable {

    private CyclicBarrier myBarrier;
    private int myThreadIndex;
    private int delay;
    private AfekGafniInterfaceRMI obj;
    private AfekGafniInterfaceRMI[] objList;
    private int totalThreads;

    MyThread(CyclicBarrier barrier, int totalThreads, int delay, AfekGafniInterfaceRMI[] objList, int threadId) {
        this.myBarrier = barrier;
        this.totalThreads = totalThreads;
        this.delay = delay;
        this.objList = objList;
        this.myThreadIndex = threadId + 1;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);  // sleep until every thread is ready to start
            //System.out.println("initial Process: " + myThreadIndex);
            this.obj = objList[myThreadIndex - 1];
            this.obj.setCandidate(delay >= 0);
            if (delay >= 0)
                Thread.sleep(100 * delay);
            if (delay >= 0) {
                System.out.println("Process " + myThreadIndex + " wake up after delay " + delay);
                this.obj.wakeup();
            }
            //System.out.println("Process " + myThreadIndex + " Done Now:" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
