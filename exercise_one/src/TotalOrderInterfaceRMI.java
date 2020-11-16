import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Queue;

public interface TotalOrderInterfaceRMI extends Remote {
    public void setId(int i) throws RemoteException;
    public int getId() throws RemoteException;
    public void setName(int i) throws RemoteException;
    public String getName() throws RemoteException;
    public void setProcessList(TotalOrder[] registryList) throws RemoteException;
    public TotalOrder[] getProcessList() throws RemoteException;
    public void setAck(ScalarClock sc) throws RemoteException;
    public HashMap<ScalarClock, Integer> getAckMap() throws RemoteException;
    public Queue<Message> getReceivedMsg() throws RemoteException;
    public String getDeliveredMsg() throws RemoteException;
    public Message broadcast(String s) throws RemoteException;
    public void receive(Message msg) throws RemoteException;
    public void deliver(Message msg) throws RemoteException;
}
