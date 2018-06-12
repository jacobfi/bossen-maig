package maig.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import maig.model.mcts.State;
import scr.SensorModel;

public class VectorSpace {

    private ArrayList<Point> data;
    private final List<TrackPoint> raw;

    private final Function tg; // The track gradient function.
    private final Function tx; // Track distance to x-coordinate.
    private final Function ty; // Track distance to y-coordinate.
    private final Function hi; // Track width in the positive direction.
    private final Function lo; // Track width in the negative direction.
    private final double[] dist; // Array of track point distances.

    public Stream<Point> track() {
        return data.subList(0, raw.size()).stream();
    }

    public double trackLength() {
        return dist[dist.length - 1];
    }

    /**
     * Vector space representation of the racing track.
     *
     * @param data a list of points on the track ordered by their distance from
     * the start line, with distances in meters and relative angles in radians.
     */
    public VectorSpace(List<TrackPoint> data) {
        this.raw = data;
        this.data = new ArrayList<>(data.size());
        ArrayList<Double> gradients = new ArrayList<>(data.size());
        ArrayList<Double> xcoords = new ArrayList<>(data.size());
        ArrayList<Double> ycoords = new ArrayList<>(data.size());
        ArrayList<Point> width = new ArrayList<>(data.size());
        double r = 0.0, prev = 0.0;
        Point p = Point.ORIGIN;
        for (TrackPoint tp : data) {
            r += tp.angle;
            // Displace p by the vector obtained from the data point.
            p = p.displace(new Vector.Polar(tp.dist - prev, r));
            this.data.add(p);
            prev = tp.dist;
            // Map the properties of p to the various track functions.
            gradients.add(r);
            xcoords.add(p.x);
            ycoords.add(p.y);
            width.add(new Point(tp.hi, tp.lo));
        }
        dist = new double[this.data.size()];
        for (int i = 0; i < this.data.size(); i++) {
            dist[i] = data.get(i).dist;
        }
        tg = new Function(IntStream.range(0, this.data.size())
                .mapToObj(i -> new Point(dist[i], gradients.get(i)))
                .collect(toList()));
        tx = new Function(IntStream.range(0, this.data.size())
                .mapToObj(i -> new Point(dist[i], xcoords.get(i)))
                .collect(toList()));
        ty = new Function(IntStream.range(0, this.data.size())
                .mapToObj(i -> new Point(dist[i], ycoords.get(i)))
                .collect(toList()));
        hi = new Function(IntStream.range(0, this.data.size())
                .mapToObj(i -> new Point(dist[i], width.get(i).x))
                .collect(toList()));
        lo = new Function(IntStream.range(0, this.data.size())
                .mapToObj(i -> new Point(dist[i], width.get(i).y))
                .collect(toList()));
    }

    /**
     * Extend the track model by one lap and erase the oldest.
     */
    public void extendTrack() {
        ArrayList<Point> aux = new ArrayList<>(
                data.subList(data.size() - raw.size(), data.size()));
        double r = 0.0, prev = 0.0;
        Point p = data.get(data.size() - 1);
        for (TrackPoint tp : raw) {
            r += tp.angle;
            p = p.displace(new Vector.Polar(tp.dist - prev, r));
            aux.add(p);
            prev = tp.dist;
        }
        data = aux;
        // Remap the x- and y-coordinate track functions.
        for (int i = 0; i < raw.size(); i++) {
            p = aux.get(i);
            tx.remap(i, p.x);
            ty.remap(i, p.y);
        }
    }

    /**
     * Transform a state from the sensor model to the vector space model.
     *
     * @param model sensor model representation of the current state.
     * @return vector space representation.
     */
    public State transform(SensorModel model) {
        double distance = Math.min(
                model.getDistanceFromStartLine(), trackLength());
        double drift = model.getTrackPosition();
        double speedX = model.getSpeed() / 3.6;
        double angle = -model.getAngleToTrackAxis();
        // Convert normalized drift to meters.
        drift *= drift > 0.0 ? hi.f(distance) : lo.f(distance);
        // The position of the car in the vector space.
        Point p = new Point(tx.f(distance), ty.f(distance));
        p = p.displace(new Vector.Polar(drift, tg.f(distance) + Math.PI / 2));
        // The velocity of the car relative to the vector space orientation.
        Vector v = new Vector.Euclidean(new Point(speedX, 0.0));
        v = v.rotate(tg.f(distance) + angle);
        // Return the state of the car in the vector space model.
        return new State(this, distance, p, v, 1, v);
    }

    /**
     * Transform a state from the vector space model to the sensor model.
     *
     * @param origin a distance from start equal to or behind the current
     * position. The closer it is to the current position, the more efficient
     * this method.
     * @param state vector space representation of the current state.
     * @return sensor model representation.
     */
    public SimulationSensorModel transform(double origin, State state) {
        double distance, drift, angle;
        int i = Arrays.binarySearch(dist, origin);
        if (i < 0) {
            i = (-i) - 2;
            if (i < 0) {
                i = 0;
            }
        }
        Point p, q = null, pos = state.position();
        double prev, d = Double.POSITIVE_INFINITY;
        do {
            p = q;
            q = data.get(i++);
            prev = d;
        } while ((d = q.distanceSquared(pos)) <= prev);
        boolean newlap = false;
        if (i >= raw.size()) {
            i -= raw.size();
            newlap = true;
        }
        int j = i < 2 ? raw.size() - (i + 1) : i - 2;
        distance = dist[j]; // Distance along track at point p.
        drift = Math.sqrt(prev); // Positive drift in m (non-normalized).
        Vector pq = new Vector.Euclidean(p, q);
        Vector pm = new Vector.Euclidean(p, pos);
        double posf = pq.determinant(pm); // Magic.
        drift /= posf > 0.0 ? hi.f(distance) : -lo.f(distance); // Normalized.
        angle = tg.f(distance) - state.velocity().angle(); // Angle to track.
        return new SimulationSensorModel(distance, drift, angle,
                state.velocity().size() * 3.6, 0.0, newlap);
    }
}
