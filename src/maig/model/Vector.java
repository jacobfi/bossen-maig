package maig.model;

public abstract class Vector {

    public static final Vector NULL_VECTOR = new Polar(0.0, 0.0);

    public abstract double x();

    public abstract double y();

    /**
     * Is in fact the length of the vector
     *
     * @return the length of the vector
     */
    public abstract double size();

    /**
     * The angle of the vector in radians
     *
     * @return the angle of the vector
     */
    public abstract double angle();

    public Vector rotate(double a) {
        return new Polar(size(), angle() + a);
    }

    public Vector scale(double x) {
        return new Polar(size() * x, angle());
    }

    public Vector perp() {
        return new Polar(size(), angle() + Math.PI * 0.5);
    }

    public Vector add(Vector v) {
        return new Euclidean(new Point(x() + v.x(), y() + v.y()));
    }

    public double dotProduct(Vector v) {
        return x() * v.x() + y() * v.y();
    }

    public double determinant(Vector v) {
        return x() * v.y() - y() * v.x();
    }

    @Override
    public String toString() {
        return String.format("[%.5f / %.5f]", size(), angle());
    }

    public static class Euclidean extends Vector {

        private final Point p;
        private Polar pv;

        public Euclidean(Point p) {
            this.p = p;
        }

        public Euclidean(Point p, Point q) {
            this.p = q.subtract(p);
        }

        private Euclidean(double size, double angle) {
            p = new Point(size * Math.cos(angle), size * Math.sin(angle));
        }

        @Override
        public double x() {
            return p.x;
        }

        @Override
        public double y() {
            return p.y;
        }

        @Override
        public double size() {
            if (pv == null) {
                pv = new Polar(p);
            }
            return pv.size;
        }

        @Override
        public double angle() {
            if (pv == null) {
                pv = new Polar(p);
            }
            return pv.angle;
        }

        @Override
        public Vector scale(double t) {
            return new Euclidean(new Point(p.x * t, p.y * t));
        }

        @Override
        public Vector perp() {
            return new Euclidean(new Point(-p.y, p.x));
        }
    }

    public static class Polar extends Vector {

        private final double size, angle;
        private Euclidean ev;

        public Polar(double size, double angle) {
            this.size = size;
            this.angle = angle;
        }

        private Polar(Point p) {
            size = Point.ORIGIN.distance(p);
            if (p.x != 0.0) {
                angle = Math.atan(p.y / p.x);
            } else {
                angle = Math.signum(p.y) * Math.PI * 0.5;
            }
        }

        @Override
        public double x() {
            if (ev == null) {
                ev = new Euclidean(size, angle);
            }
            return ev.p.x;
        }

        @Override
        public double y() {
            if (ev == null) {
                ev = new Euclidean(size, angle);
            }
            return ev.p.y;
        }

        @Override
        public double size() {
            return size;
        }

        @Override
        public double angle() {
            return angle;
        }
    }
}
