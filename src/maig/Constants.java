package maig;

public class Constants {

    // Forward model time-step (in seconds).
    public static final double TIME_STEP = 0.25;

    // Timeout for a single MCTS run (milliseconds).
    public static final double TIMEOUT = 15;

    // Limit on maximum speed by limiting the gear.
    public static final int GEAR_LIMIT = 6;

    // Track position boundary for terminal states.
    public static final double OFF_COURSE = 5.3;
    // Track position boundary for terminal states in simulations.
    public static final double OFF_TRACK = 0.7;
    // Score penalty factor when the car is within OFF_TRACK.
    public static final double PENALTY_FACTOR = 0.4;

    // Upper bound on acceleration (m/s^2). Used for MCTS evaluation heuristic.
    public static final double ACC_MAX = 10.0;

    // Speed impact on the radius of the steering circle
    //public static final double R_FACTOR = 1.015;
    public static final double R_FACTOR = 0.999;
    public static final double BRAKE_FACTOR = 3.9;
    //public static final double BRAKE_FACTOR = 20;

    // MCTS C-value.
    public static final double MCTS_C = 0.7;
    // MCTS simulation depth.
    public static final int SIM_DEPTH = 3;

    // Constants determining the action space.
    public static final double GAS_INTERVAL = 0.2;
    public static final double STEERING_INTERVAL = 0.1;
    public static final int GAS_N = 5;
    public static final int STE_N = 10;
}
