package maig.model.mcts;

import java.util.Locale;

public class Action {

    public static final Action NONE = new Action(0.0, 0.0);
    public static final Action BRAKE = new Action(-1.0, 0.0);

    // Absolute values. Between actions, a state is always quiescent,
    // i.e. 0 gas and 0 steering.
    public final double gas; // Negative gas represents braking.
    public final double steering;

    public Action(double gas, double steering) {
        this.gas = gas;
        this.steering = steering;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[%2.1f,%2.2f]", gas, steering);
    }
}
