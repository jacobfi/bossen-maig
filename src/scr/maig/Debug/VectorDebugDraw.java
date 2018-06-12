package scr.maig.Debug;

import maig.model.*;
import maig.model.Point;
import scr.maig.DataGathering.TrackData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.*;

public class VectorDebugDraw extends JPanel {

    private final TrackData td;
    private int position = 0;
    private double direction;
    private double drift;
    private boolean dirty = false;

    private static ArrayList<Point> DebugPoints = new ArrayList<>();

    public VectorDebugDraw(TrackData td) {
        this.td = td;
        setSize(new Dimension(800, 800));
        setVisible(true);
    }

    public static void addDebugPoint(double x, double y) {
        DebugPoints.add(new Point(x, y));
    }

    public void update(int position) {update(position, 0, 0);}

    public void update(int distance, double direction, double drift) {
        this.position = distance;
        this.direction = direction;
        this.drift = drift;
        dirty = true;
    }

    public void paint(Graphics g) {
        if(dirty) {
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.lightGray);
            dirty = false;
        }
        Graphics2D g2d = (Graphics2D)g;
        AffineTransform state = g2d.getTransform();
        int y = getHeight()/2;
        int x = getWidth()/2;
        Vector v = new Vector(0, 1);
        g2d.rotate(-this.direction, x, y);
        for(TrackPoint d : td.getNextPoints(position, 3000)) {
            if(d == null)
                continue;
            int trackWidth = 4 * (int)(d.hi + d.lo);
            int trackOffset = (int)Math.round((-trackWidth / 2) + (trackWidth/2 * drift));
            x += Math.round(v.x * 1);
            y -= Math.round(v.y * 1);
            g2d.translate(x, y);
            g2d.rotate(-d.angle / 2.0);
            g2d.translate(trackOffset, 0);
            g2d.fillRect(0, 0, trackWidth, 2);
            g2d.translate(-trackOffset, 0);
            g2d.translate(-x, -y);
        }
        g2d.setTransform(state);

        g2d.translate(getWidth()/2, getHeight()/2);
        g2d.setColor(Color.red);
        g2d.fillRect(0, 0, 5, 10);

        g2d.setColor(Color.blue);
        for(Point p : DebugPoints)
            g2d.drawOval((int)Math.round(p.x), (int)Math.round(p.y), 4, 4);

        DebugPoints.clear();
    }

    private class Vector {
        public final double x;
        public final double y;

        Vector(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        public Vector rotate(double n) {
            return new Vector(
                    x * Math.cos(n) - y * Math.sin(n),
                    x * Math.sin(n) + y * Math.cos(n)
            );
        }
    }
}