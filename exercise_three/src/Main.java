import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Main {
    public static void main(String[] args) throws RemoteException, NotBoundException, AlreadyBoundException {
        Registry registry = LocateRegistry.createRegistry(1099);
        int TOTAL_THREADS = 0;
        String caseid = args[0];
        String prefix = "test";
        String suffix = ".txt";
        String path = prefix + caseid + suffix;

        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            TOTAL_THREADS = Integer.parseInt(in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        AfekGafniInterfaceRMI[] objList = new AfekGafniInterfaceRMI[TOTAL_THREADS];
        for (int i = 0; i < TOTAL_THREADS; i++) {
            AfekGafni obj = new AfekGafni();
            obj.setId(i+1);
            obj.setName(i+1);
            obj.setObjList(objList);
            registry.bind("process-" + (i+1), obj);
            objList[i] = (AfekGafniInterfaceRMI) registry.lookup("process-" + (i+1));
        }

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

