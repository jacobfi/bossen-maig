package maig.model.test;

import java.util.ArrayList;
import maig.mcts.Node;
import maig.model.mcts.Action;
import maig.model.mcts.State;
import maig.util.TrackDataController;
import scr.SensorModel;

public class PerformanceDriver extends TrackDataController {

    private final ArrayList<Double> dist, drift, angle;
    private final ArrayList<Integer> iter;

    public PerformanceDriver() {
        super();
        dist = new ArrayList<>();
        drift = new ArrayList<>();
        angle = new ArrayList<>();
        iter = new ArrayList<>();
    }

    @Override
    public scr.Action control(SensorModel sensors) {
        scr.Action a = new scr.Action();
        if (sensors.getCurrentLapTime() > 0) {
            State s = vs.transform(sensors);
            Node<Action, State> mcts = new Node<>(s);
            mcts.run(System.currentTimeMillis() + 10);
            //iter.add(mcts.n);
            /*SensorModel m = vs.transform(sensors.getDistanceFromStartLine(), s);
             dist.add(Math.abs(sensors.getDistanceFromStartLine() - m.getDistanceFromStartLine()));
             drift.add(Math.abs(sensors.getTrackPosition() - m.getTrackPosition()));
             angle.add(Math.abs(sensors.getAngleToTrackAxis() - m.getAngleToTrackAxis()));*/
        }
        a.gear = 1;
        a.accelerate = 1.0;
        //a.steering = -0.01;
        a.steering = 0.0;
        if (sensors.getCurrentLapTime() >= 5) {
            a.restartRace = true;
        }
        return a;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        System.out.printf("avg. iterations: %.3f\n", iter.stream().mapToInt(i -> i).average().getAsDouble());
        /*System.out.printf("avg. dist error: %.10f\n", dist.stream().mapToDouble(d -> d).average().getAsDouble());
         System.out.printf("avg. drift error: %.10f\n", drift.stream().mapToDouble(d -> d).average().getAsDouble());
         System.out.printf("avg. angle error: %.10f\n", angle.stream().mapToDouble(d -> d).average().getAsDouble());*/
    }
}
