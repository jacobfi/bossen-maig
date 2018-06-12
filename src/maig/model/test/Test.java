package maig.model.test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.*;

import maig.model.Point;
import maig.model.TrackPoint;
import maig.model.VectorSpace;
import scr.maig.Debug.VectorDebugDraw;
import scr.maig.DataGathering.TrackData;

public class Test {

    private static VectorSpace vs;
    private static double bound;
    private static VectorSpace best_vs;

    public static void main(String[] args) {
        //vs = createVectorSpace("track.mdl", (Math.PI * Math.E * 1.24));
        vs = createVectorSpace("track.mdl");
        best_vs = createVectorSpace("best_track.mdl");
        double xmax = vs.track().mapToDouble(p
                -> Math.abs(p.x)).max().getAsDouble();
        double ymax = vs.track().mapToDouble(p
                -> Math.abs(p.y)).max().getAsDouble();
        bound = Math.max(xmax, ymax);
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        TestDrawing canvas = new TestDrawing();
        window.add(canvas);
        window.setSize(canvas.getSize());
        window.setVisible(true);
    }

    public static VectorSpace createVectorSpace(String fileName) {
        TrackData td;
        try {
            FileInputStream fi = new FileInputStream(fileName);
            ObjectInputStream i = new ObjectInputStream(fi);
            td = (TrackData) i.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
        ArrayList<TrackPoint> arr = td.getNextPoints(1, 50_000);
        double magicConstant = (Math.PI*2) / arr.stream().mapToDouble(tp -> tp.angle).sum();
        return new VectorSpace(arr.stream()
                .map(tp -> new TrackPoint(tp.dist, tp.angle*magicConstant, tp.hi, tp.lo))
                .collect(Collectors.toList()));
    }

    private static class TestDrawing extends VectorDebugDraw {

        TestDrawing() {
            super(null);
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.RED);
            Graphics2D g2d = (Graphics2D) g;
            int lo = Math.min(getWidth(), getHeight());
            double nx = lo / (2 * bound);
            double ny = lo / (2 * bound);
            Consumer<Point> draw = (p) -> {
                g2d.fillOval(
                        (int) (p.x * nx) + getWidth() / 2,
                        getHeight() - ((int) (p.y * ny) + getHeight() / 2),
                        2, 3);
            };
            best_vs.track().forEach(draw);
            //g2d.rotate(Math.toRadians(1.5), getWidth()/2, getHeight()/2);
            g.setColor(Color.BLACK);
            vs.track().forEach(draw);
        }
    }
}
