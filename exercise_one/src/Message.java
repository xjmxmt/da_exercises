import java.io.Serializable;

public class Message implements Serializable{
    private static final long serialVersionUID = 7526471155622776147L;
    private String content;
    public ScalarClock scalarClock;

    public void setMessage(String s, ScalarClock sc) {
        this.content = s;
        this.scalarClock = sc;
    }

    @Override
    public String toString() {
        return "(" + content + ", " + scalarClock + ")";
    }
}
