import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

public interface MaekawaInterfaceRMI extends Remote {
    public void setId(int i) throws RemoteException;
    public int getId() throws RemoteException;
    public void setName(int i) throws RemoteException;
    public String getName() throws RemoteException;
    public void setComponents(MaekawaComponent[] l) throws RemoteException;
    public void initResources(int n) throws RemoteException;
    public void buildRequestSet(int total) throws RemoteException;
    public void request(int i) throws RemoteException;
    public void receiveRequest(int sender, int i) throws RemoteException;
    public void receiveAck(int sender, int i) throws RemoteException;
    public void release(int i) throws RemoteException;
    public void receiveRelease(int i) throws RemoteException;
}
