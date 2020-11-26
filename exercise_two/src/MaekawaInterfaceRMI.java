import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public interface MaekawaInterfaceRMI extends Remote {
    public void setId(int i) throws RemoteException;
    public int getId() throws RemoteException;
    public void setName(int i) throws RemoteException;
    public String getName() throws RemoteException;
    public void setTimer(AtomicInteger timer) throws RemoteException;
    public void setComponents(MaekawaInterfaceRMI[] l) throws RemoteException;
    public void setMsg(Message msg) throws RemoteException;
    public void initResources(int n) throws RemoteException;
    public void buildRequestSet(int total) throws RemoteException;
    public void request() throws RemoteException;
    public void receiveRequest(ScalarClock scalarClock, MaekawaInterfaceRMI sender) throws RemoteException;
    public void doSomeOperations() throws RemoteException;
    public void receiveGrant() throws RemoteException;
//    public void receiveAck(int sender, int i) throws RemoteException;
    public void release() throws RemoteException;
//    public void receiveRelease(int i) throws RemoteException;
    public void receivePostpone() throws RemoteException;
    public void receiveInquire(MaekawaInterfaceRMI sender) throws RemoteException;
    public void receiveRelinquish() throws RemoteException;
}
