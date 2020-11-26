import java.io.Serializable;

public class ScalarClock implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    public long time;
    public int processId;

    public ScalarClock() {

    }

    public ScalarClock(long t, int i) {
        this.time = t;
        this.processId = i;
    }

    public void setScalarClock(long t, int i) {
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

    @Override
    public int hashCode() {
        return (time + "").hashCode() + (processId + "").hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScalarClock)) return false;
        ScalarClock sc = (ScalarClock) obj;
        if (this.time == sc.time && this.processId == sc.processId) return true;
        else return false;
    }

}