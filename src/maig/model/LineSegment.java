package maig.model;

public class LineSegment {

    public final Point p;
    public final Vector v;

    public LineSegment(Point p, Vector v) {
        this.p = p;
        this.v = v;
    }

    public LineSegment(Point p, Point q) {
        this.p = p;
        this.v = new Vector.Euclidean(p, q);
    }

    public Point head() {
        return p.displace(v);
    }

    public Point tail() {
        return p;
    }
}
