package scr.maig.mcts;

import static maig.Constants.GEAR_LIMIT;
import static maig.Constants.TIMEOUT;
import static maig.Constants.TIME_STEP;
import maig.model.mcts.Action;
import maig.mcts.Node;
import maig.model.mcts.State;
import maig.util.TrackDataController;
import scr.SensorModel;
import scr.maig.Debug.DebugDraw;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class MonteCarloDriver extends TrackDataController {

    private Node<Action, State> mcts;
    private Action decision;
    private double time = 0.0;

    private final DebugDraw debugDrawing;
    private final JFrame window;
    private State next;

    private static int ticks = 0;
    private static ArrayList<Double> distances = new ArrayList<>();
    private SensorModel sns;


    public MonteCarloDriver() {
        super();
        vs.extendTrack();
        window = new JFrame();
        window.setLocation(800, 0);
        window.setVisible(true);
        debugDrawing = new DebugDraw(vs);
        window.setSize(debugDrawing.getSize());
        window.add(debugDrawing);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void reset() {
        System.out.println("Restarting");
        distances.add(sns.getDistanceRaced());
        System.out.println("Distance " + sns.getDistanceRaced());
        if(distances.size() >= 10) {
            distances.forEach(d -> System.out.println(d));
            System.out.println(distances.stream().mapToDouble(d->d).average().getAsDouble());
        }
    }

    @Override
    public scr.Action control(SensorModel sensors) {
        sns = sensors;
        double laptime = sensors.getCurrentLapTime();
        if (laptime >= 0.0 && laptime < time) {
            // We have completed a lap. Extend the track buffer.
            vs.extendTrack();
            mcts = null;
            time = 0.0;
        }

        if (debugDrawing != null) {
            debugDrawing.update((int) sensors.getDistanceFromStartLine(), sensors.getAngleToTrackAxis(), sensors.getTrackPosition());
            debugDrawing.repaint();
        }

        if (mcts == null) {
            mcts = new Node<>(vs.transform(sensors));
        }
        if (laptime - time >= TIME_STEP) {
            time = laptime;
            decision = mcts.getDecision();
            next = vs.transform(sensors);
            next.applyAction(decision);

            if (debugDrawing != null) {
                DebugDraw.clearDebug();
                DebugDraw.addDebugString(Double.toString(mcts.getBestChildValue()));
            }

            mcts = new Node<>(next);
        }
        mcts.run(System.currentTimeMillis() + TIMEOUT);
        scr.Action a = new scr.Action();
        if (decision != null) {
            a.accelerate = decision.gas >= 0.0 ? decision.gas : 0.0;
            a.brake = decision.gas <= 0.0 ? -decision.gas : 0.0;
            a.steering = decision.steering;
            a.gear = transmission(sensors.getGear(), sensors.getRPM());
        }

        if(ticks > 10_000) {
            ticks = 0;

            a.restartRace = true;
        }
        ticks++;

        return a;
    }

    private int transmission(int gear, double rpm) {
        if (gear == 0) {
            gear = 1;
        }
        if (rpm > 8000 && gear < GEAR_LIMIT) {
            gear++;
        } else if (rpm < 3000 && gear > 1) {
            gear--;
        }
        return gear;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        window.dispose();
    }
}
