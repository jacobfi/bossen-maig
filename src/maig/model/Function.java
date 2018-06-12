package maig.model;

import java.util.Arrays;
import java.util.List;

public class Function {

    private final double[] xs;
    private final double[] ys;

    /**
     * A continuous mapping between the range and domain of the given points.
     *
     * @param points a list of points. The list <i>must</i> be ordered by x
     * values.
     */
    public Function(List<Point> points) {
        xs = new double[points.size()];
        ys = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xs[i] = points.get(i).x;
            ys[i] = points.get(i).y;
        }
    }

    /**
     * Interpolates between adjacent values in the range to form a continuous
     * mapping of the domain.
     *
     * @param x the x value.
     * @return the y value.
     */
    public double f(double x) {
        int i = Arrays.binarySearch(xs, x);
        if (i >= 0) {
            return ys[i];
        }
        i = (-i) - 2;
        if (i < 0) {
            return 0.0;
        }
        int j = i + 1;
        double dy = ys[j] - ys[i];
        double dx = (x - xs[i]) / (xs[j] - xs[i]);
        return dx * dy + ys[i];
    }

    public void remap(int i, double y) {
        ys[i] = y;
    }
}
