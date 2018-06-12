package maig.model;

public class Line extends LineSegment {

    public Line(Point p, Vector v) {
        super(p, v);
    }

    public Line(Point p, Point q) {
        super(p, q);
    }

    public Point get(double t) {
        return p.displace(v.scale(t));
    }

    public Point intersection(Line l) {
        Vector pq = new Vector.Euclidean(p, l.p);
        double a = pq.determinant(l.v) / v.determinant(l.v);
        return get(a);
    }

    public boolean parallel(Line l) {
        return v.determinant(l.v) == 0;
    }

    public boolean orthogonal(Line l) {
        return v.dotProduct(l.v) == 0;
    }
}
