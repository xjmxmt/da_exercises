import java.io.Serializable;

public class ScalarClock implements Serializable{
    private static final long serialVersionUID = 7526471155622776147L;
    public int time;
    public int processId;

    public ScalarClock() {

    }

    public ScalarClock(int t, int i) {
        this.time = t;
        this.processId = i;
    }

    public void setScalarClock(int t, int i) {
        this.time = t;
        this.processId = i;
    }

    public Boolean isSmallerThan(ScalarClock ts) {
        if (ts == null) return true;
        else if (this.time < ts.time) return true;
        else if (this.time == ts.time && this.processId <= ts.processId) return true;
        else return false;

    }

    @Override
    public String toString() {
        return "(" + time + ", " + processId + ")";
    }
}
