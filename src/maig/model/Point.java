package maig.model;

public class Point {

    public static final Point ORIGIN = new Point(0, 0);

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point displace(Vector v) {
        return new Point(x + v.x(), y + v.y());
    }

    public Point rotate(double a, double r, double offset) {
        double s = Math.sin(a * 0.5) * r * 2;
        double b = (Math.PI + a) * 0.5;
        return displace(new Vector.Polar(s, b + offset));
    }

    public Point add(Point p) {
        return new Point(x + p.x, y + p.y);
    }

    public Point subtract(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public double distanceSquared(Point p) {
        return (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y);
    }

    public double distance(Point p) {
        return Math.sqrt(distanceSquared(p));
    }

    public Point projectionOnSegment(Point p, Point q) {
        double l2 = p.distanceSquared(q);
        if (l2 == 0) {
            return p;
        }
        Vector pq = new Vector.Euclidean(p, q);
        double t = new Vector.Euclidean(p, this).dotProduct(pq) / l2;
        if (t < 0.0) {
            return p; // Beyond the 'p' end of the segment.
        } else if (t > 1.0) {
            return q; // Beyond the 'q' end of the segment.
        } else {
            return p.displace(pq.scale(t));
        }
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
