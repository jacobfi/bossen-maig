package maig.model;

import java.io.Serializable;

public class TrackPoint implements Serializable {

    public final double dist;
    public final double angle;
    public final double hi, lo;

    public TrackPoint(double dist, double angle, double hi, double lo) {
        this.dist = dist;
        this.angle = angle;
        this.hi = hi;
        this.lo = lo;
    }
}
