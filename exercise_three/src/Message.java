import java.io.Serializable;

public class Message implements Serializable, Comparable<Message>{
    private static final long serialVersionUID = 7526471155622776147L;
    public int level;
    public int id;
    public int sender;

    public Message(int level, int id, int sender) {
        this.level = level;
        this.id = id;
        this.sender = sender;
    }

    public void setMessage(int level, int id, int sender) {
        this.level = level;
        this.id = id;
        this.sender = sender;
    }

    @Override
    public int compareTo(Message o) {
        if (this.level > o.level)
            return 1;
        if (this.level < o.level)
            return -1;
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toString() {
        return "(" + level + ", " + id + ")";
    }
}
