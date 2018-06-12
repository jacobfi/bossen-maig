package maig.learning;

import maig.model.Vector;
import maig.model.mcts.State;
import maig.util.TrackDataController;
import scr.Action;
import scr.SensorModel;

public class ForwardStepDriver extends TrackDataController {

    private double time = 0.0;
    private State start;
    private boolean step1 = true;
    private maig.model.mcts.Action a1 = new maig.model.mcts.Action(0.4, 0.1);
    private final maig.model.mcts.Action[] aset = new maig.model.mcts.Action[]{
        new maig.model.mcts.Action(0.4, -0.1),
        new maig.model.mcts.Action(0.4, -0.1),
        new maig.model.mcts.Action(0.4, 0.1),
        new maig.model.mcts.Action(0.4, 0.1)
    };
    private int asix = aset.length - 2;

    @Override
    public Action control(SensorModel sensors) {
        Action a = new Action();
        a.gear = 1;
        if (sensors.getCurrentLapTime() - time >= 1.0) {
            time = sensors.getCurrentLapTime();
            if (time > 3.0) {
                if (start != null) {
                    State end = vs.transform(sensors);
                    Vector m = start.velocity();
                    Vector n = end.velocity();
                    System.out.println(n.angle() - m.angle());
                }
                start = vs.transform(sensors);
                asix++;
                asix %= aset.length;
            }
        }
        if (time >= 3.0) {
            if (step1) {
                if (time >= 4.0) {
                    step1 = false;
                }
                a.accelerate = a1.gas;
                a.steering = a1.steering;
            } else {
                a.accelerate = aset[asix].gas;
                a.steering = aset[asix].steering;
            }
        } else {
            a.accelerate = 1.0;
            a.steering = 0.0;
        }
        return a;
    }
}
