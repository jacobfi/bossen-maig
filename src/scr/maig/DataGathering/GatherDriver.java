package scr.maig.DataGathering;

import maig.model.Point;
import maig.model.TrackPoint;
import scr.SensorModel;
import scr.SimpleDriver;
import scr.Action;
import scr.maig.Debug.VectorDebugDraw;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Mathias Vielwerth on 04-11-2014.
 */
public class GatherDriver extends SimpleDriver {

    TrackData trackData = new TrackData();
    double distance = 0;
    ArrayList<Double> angles = new ArrayList<>();
    VectorDebugDraw simpleDrawing;
    JFrame window;

    int angle = 1;

    double magic;

    final boolean WRITE_TRACK = true;
    private boolean started = false;

    public GatherDriver() {
        if(WRITE_TRACK) {
            maxSpeed = 33;
            maxSpeedDist = 0;
        }
    }

    @Override
    public void shutdown() {
        if(WRITE_TRACK) {
            try {
                System.out.println("Writing track");
                FileOutputStream fo = new FileOutputStream("track.mdl");
                ObjectOutputStream o = new ObjectOutputStream(fo);
                o.writeObject(trackData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            window.dispose();
        }
        super.shutdown();
    }

    private SensorModel sne;
    @Override
    public Action control(SensorModel sensors) {
        if(sensors.getDistanceFromStartLine() > 0 && sensors.getDistanceFromStartLine() < 100 && sensors.getDistanceRaced() < 1000 && !started)
            started = true;
        else if(sensors.getDistanceFromStartLine() >= 0 && sensors.getDistanceFromStartLine() < 100 && sensors.getDistanceRaced() > 1000 && started)
            started = false;

        sne = sensors;

        if(!started && WRITE_TRACK)
            return super.control(sensors);
        
        if(!WRITE_TRACK) {
            simpleDrawing.update((int) sensors.getDistanceFromStartLine());
            simpleDrawing.repaint();
        }

        double[] edges = sensors.getTrackEdgeSensors();
        magic = sensors.getAngleToTrackAxis();

        double direction = Math.signum(sensors.getAngleToTrackAxis());
        double leftAngle = getTrackAngle(edges[0], edges[1], edges[2], direction);
        double rightAngle = getTrackAngle(edges[18], edges[17], edges[16], direction);

        double trackAngle = Math.min(1000, Math.max(-1000, (leftAngle + rightAngle) / 2));

        trackData.put(new TrackPoint(sensors.getDistanceFromStartLine(), trackAngle, edges[0], edges[18]));

        return super.control(sensors);
    }

    private double getTrackAngle(double sensor0, double sensor1, double sensor2, double direction) {
        Point[] circlePoints = sensorToPoints(sensor0, sensor1, sensor2);
        Point center = calcuateCircleCenter(circlePoints);

        double radius = center.distance(circlePoints[0]);

        return (1/radius) * direction;
    }

    private Point[] sensorToPoints(double sensor0, double sensor1, double sensor2) {
        return new Point[] {
        new Point(0, sensor0),
                new Point(sensor1 * Math.cos(Math.toRadians(90-angle)), sensor1 * Math.sin(Math.toRadians(90-angle))),
                new Point(sensor2 * Math.cos(Math.toRadians(90-angle*2)), sensor2 * Math.sin(Math.toRadians(90-angle*2)))
        };
    }

    private Point calcuateCircleCenter(Point[] p) {
        Point deltaA = calculateDelta(p[0], p[1]);
        Point deltaB = calculateDelta(p[1], p[2]);

        // Slopes
        double sA = deltaA.y / deltaA.x;
        double sB = deltaB.y / deltaB.x;

        double centerX = (sA*sB*(p[0].y - p[2].y) + sB*(p[0].x + p[1].x) - sA*(p[1].x + p[2].x)) / (2* (sB-sA));
        double centerY = -1*(centerX - (p[0].x+p[1].x)/2)/sA + (p[0].y + p[1].y)/2;

        return new Point (centerX, centerY);
    }

    private Point calculateDelta(Point p1, Point p2) {
        return new Point(
                p2.x - p1.x,
                p2.y - p1.y
        );
    }

    @Override
    public float[] initAngles() {
        if(!WRITE_TRACK) {
            try {
                FileInputStream fi = new FileInputStream("track.mdl");
                ObjectInputStream i = new ObjectInputStream(fi);
                trackData = (TrackData) i.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("You have not gathered any track data yet.");
            }
            window = new JFrame();
            window.setVisible(true);
            simpleDrawing = new VectorDebugDraw(trackData);
            window.setSize(simpleDrawing.getSize());
            window.add(simpleDrawing);
            window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        float[] angs = super.initAngles();
        angs[1] = -90 + angle;
        angs[2] = -90 + angle*2;
        angs[17] = 90 - angle;
        angs[16] = 90 - angle*2;
        return angs;
    }
}
