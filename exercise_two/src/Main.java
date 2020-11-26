import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URL;


public class Main {
    public static void main(String[] args) throws RemoteException, NotBoundException, AlreadyBoundException {
        MaekawaMain maekawaMain = new MaekawaMain();
        Registry registry = LocateRegistry.createRegistry(1099);
        int TOTAL_THREADS = Integer.parseInt(args[0]);
        URL location = MaekawaMain.class.getProtectionDomain().getCodeSource()
                .getLocation();
        String path = location.getFile();
        String rsPath = path + "requestset_"+TOTAL_THREADS+".txt";
        MaekawaComponent[] objList = new MaekawaComponent[TOTAL_THREADS];
        AtomicInteger timer = new AtomicInteger(0);
        Message msg = new Message(0);
        for (int i = 0; i < TOTAL_THREADS; i++) {
            MaekawaComponent obj = new MaekawaComponent();
            obj.setId(i);
            obj.setName(i);
            obj.setTimer(timer);
            obj.setMsg(msg);
            obj.setObjList(objList);
            obj.buildRequestSet(TOTAL_THREADS);
            registry.bind("process-" + (i), obj);
            objList[i] = (MaekawaComponent) registry.lookup("process-" + (i));
        }

//        CyclicBarrier myBarrier = new CyclicBarrier(TOTAL_THREADS, new Runnable() {
//            @Override
//            public void run() {
//                for (MaekawaComponent obj : objList) {
//                    try {
//                        System.out.println("name: " + obj.getName() + ", no_grants: " + obj.no_grants.intValue() + ", current grants: " + obj.current_grant + ", " + obj.receivedRqst);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
//                System.out.println("All processes done.");
//                System.exit(0);
//            }
//        });

//        // read request sets
//        HashMap<Integer, int[]> requestSets = new HashMap<>();
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(rsPath));
//            Integer i = 0;
//            String str;
//            while ((str = in.readLine()) != null) {
//                requestSets.put(i, Arrays.stream(str.split(" ")).mapToInt(Integer::parseInt).toArray());
//                i += 1;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        for (int i = 0; i < TOTAL_THREADS; i++) {
//            ProcessBuilder pb = new ProcessBuilder("java " + path + "MaekawaMain " + TOTAL_THREADS + " " + rsPath + " " + i);
//            pb.redirectErrorStream(true);
//            try {
//                Process process = pb.start();
////                InputStream inputStream = process.getInputStream();
////                BufferedReader bufferedReader = new BufferedReader(
////                        new InputStreamReader(inputStream));
////                String line = "";
////                while ((line = bufferedReader.readLine()) != null) {
////                    System.out.println(INFO + line);
////                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
