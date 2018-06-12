package scr.maig.Debug;

import jdk.nashorn.internal.runtime.Debug;
import maig.model.*;
import maig.model.Point;
import maig.model.mcts.State;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Created by Mathias Vielwerth on 11-11-2014.
 */
public class DebugDraw extends JPanel {
    private final VectorSpace vs;
    private final double bound;
    private int distance;
    private double direction;
    private double drift;

    private static ArrayList<Point> DebugBluePoints = new ArrayList<>();
    private static ArrayList<Point> DebugRedPoints = new ArrayList<>();
    private static ArrayList<Point> DebugGreenPoints = new ArrayList<>();
    private static ArrayList<String> DebugText = new ArrayList<>();

    public DebugDraw(VectorSpace vs) {
        setSize(800, 800);
        setVisible(true);
        this.vs = vs;

        double xmax = vs.track().mapToDouble(p
                -> Math.abs(p.x)).max().getAsDouble();
        double ymax = vs.track().mapToDouble(p
                -> Math.abs(p.y)).max().getAsDouble();
        bound = Math.max(xmax, ymax);
    }

    public static void addDebugBluePoint(double x, double y) { synchronized (DebugBluePoints) {DebugBluePoints.add(new Point(x, y));} }
    public static void addDebugRedPoint(double x, double y) { synchronized (DebugRedPoints) {DebugRedPoints.add(new Point(x, y));} }
    public static synchronized void clearDebug() {synchronized (DebugBluePoints) {DebugRedPoints.clear(); DebugBluePoints.clear(); DebugText.clear();}}
    public static synchronized void addDebugString(String s) {synchronized (DebugText) {DebugText.add(s);}}

    public static void nextStep() {
        DebugGreenPoints = (ArrayList<Point>)DebugBluePoints.clone();
        clearDebug();
    }

    public void update(int distance, double direction, double drift) {
        this.distance = distance;
        this.direction = direction;
        this.drift = drift;
    }

    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        Graphics2D g2d = (Graphics2D) g;

        int lo = Math.min(getWidth(), getHeight()) * 5;
        double nx = lo / (2 * bound);
        double ny = lo / (2 * bound);

        SimulationSensorModel sensor = new SimulationSensorModel(this.distance, this.drift, this.direction, 0, 0, false);
        State state = vs.transform(sensor);

        double rX = state.position().x * nx + getWidth() / 2;
        double rY = getHeight() - ((state.position().y * ny) + getHeight() /2);

        g.setColor(Color.GRAY);
        AffineTransform transform = g2d.getTransform();
        g2d.translate((int) (-state.position().x * nx), ((int) (state.position().y * ny)));
        //g2d.rotate(-direction, rX, rY);

        final Point[] pp = {new Point(0, 0)};
        final int[] size = {2, (int)(16 * ny)};
        Consumer<Point> draw = (p) -> {
            int x = (int) (p.x * nx) + getWidth() / 2;
            int y = getHeight() - ((int) (p.y * ny) + getHeight() / 2);
            Vector v = new Vector.Euclidean(pp[0].x < p.x ? pp[0].subtract(p) : p.subtract(pp[0]));

            g2d.translate(x, y);
            g2d.rotate(-v.angle());

            g2d.fillRect(0, -size[1]/2, size[0], size[1]);
            pp[0] = p;

            g2d.rotate(v.angle());
            g2d.translate(-x, -y);
        };
        vs.track().forEach(draw);

        g.setColor(Color.YELLOW);
        pp[0] = new Point(0, 0);
        size[0] = 2;
        size[1] = 2;
        draw.accept(state.position());

        g.setColor(Color.GREEN);
        pp[0] = new Point(0, 0);
        size[0] = 1;
        size[1] = 1;
        synchronized (DebugBluePoints) {
            DebugGreenPoints.stream().forEach(draw);
            g.setColor(Color.BLUE);
            DebugBluePoints.stream().forEach(draw);

            g.setColor(Color.RED);
            pp[0] = new Point(0, 0);
            size[0] = 2;
            size[1] = 2;

            synchronized (DebugRedPoints) {
                DebugRedPoints.stream().forEach(draw);
            }

            g.setColor(Color.BLACK);
            g2d.setTransform(transform);
            IntStream.range(0, DebugText.size()).forEach(i -> {
                g2d.drawString(DebugText.get(i), (i * 100) + 40, getHeight() - 20);
            });
        }
        //System.out.println("-----");
    }
}
