import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AfekGafniInterfaceRMI extends Remote {
    public void setId(int i) throws RemoteException;
    public int getId() throws RemoteException;
    public void setName(int i) throws RemoteException;
    public String getName() throws RemoteException;
    public void setObjList(AfekGafniInterfaceRMI[] objList) throws RemoteException;
    public void setCandidate(boolean b) throws RemoteException;
    public void wakeup() throws RemoteException;
    public void send() throws RemoteException;
    public void receive(Message msg) throws RemoteException;
}
