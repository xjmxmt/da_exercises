import java.io.Serializable;

public class Message implements Serializable{
    private static final long serialVersionUID = 7526471155622776147L;
    private int content;

    public Message(int i){
        this.content=i;
    }

    public int getMsg() {
        return content;
    }

    public void setMsg(int i) {
        this.content=i;
    }

    public void incrementContent() {
        content += 1;
    }

    public void printMsg(ScalarClock scalarClock) {
        System.out.println("Process-" + scalarClock.processId + " write in critical region at timestamp "
                + scalarClock.time + ", message = " + content);
    }

}
