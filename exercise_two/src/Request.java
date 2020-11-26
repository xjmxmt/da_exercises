import java.io.Serializable;
import java.rmi.RemoteException;

public class Request {
    private static final long serialVersionUID = 7526471155622776147L;
    private ScalarClock scalarClock;
    private MaekawaInterfaceRMI sender;

    public Request(ScalarClock scalarClock, MaekawaInterfaceRMI sender) {
        this.scalarClock = scalarClock;
        this.sender = sender;
    }

    public ScalarClock getScalarClock() {
        return scalarClock;
    }

    public MaekawaInterfaceRMI getSender() {
        return sender;
    }

    @Override
    public String toString() {
        String name = "";
        try {
            name = sender.getName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "ScalarClock: " + scalarClock + ", Sender: " + name;
    }
}
