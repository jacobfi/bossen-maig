package maig.model.mcts;

import java.util.stream.IntStream;
import static maig.Constants.*;
import maig.model.LineSegment;
import maig.model.Point;
import maig.model.SimulationSensorModel;
import maig.model.Vector;
import maig.model.VectorSpace;

public class State implements maig.mcts.State<Action> {

    private final double origin; // Original distance from start.
    private final VectorSpace vs; // The model this state is represented in.
    private Point position;
    private Vector velocity;
    private double depth;

    private Action prevAction = defaultAction();
    private SimulationSensorModel sensor;
    private final Vector oldVelocity;

    public State(VectorSpace vs, double origin, Point position, Vector velocity,
            double depth, Vector oldVelocity) {
        this.vs = vs;
        this.origin = origin;
        this.position = position;
        this.velocity = velocity;
        this.depth = depth;
        this.oldVelocity = oldVelocity;
    }

    public Point position() {
        return position;
    }

    public Vector velocity() {
        return velocity;
    }

    public LineSegment motion() {
        return new LineSegment(position, velocity);
    }

    @Override
    public State copy() {
        return new State(vs, origin, position, velocity, depth, oldVelocity);
    }

    private static final Action[][][] actions;
    private static final int GAS_FACTOR = (int) (1 / GAS_INTERVAL);
    private static final int STE_FACTOR = (int) (1 / STEERING_INTERVAL);

    static {
        final Action[][] am = new Action[GAS_N * 2 + 1][STE_N * 2 + 1];
        for (int i = -GAS_N; i <= GAS_N; i++) {
            for (int j = -STE_N; j <= STE_N; j++) {
                am[i + GAS_N][j + STE_N]
                        = new Action(i * GAS_INTERVAL, j * STEERING_INTERVAL);
            }
        }
        actions = new Action[am.length][][];
        IntStream.range(0, am.length).forEach(i -> {
            actions[i] = new Action[am[i].length][];
            IntStream.range(0, am[i].length).forEach(j -> {
                actions[i][j] = IntStream.rangeClosed(
                        Math.max(i - 1, 0), Math.min(i + 1, am.length - 1)
                ).mapToObj(ai -> IntStream.rangeClosed(
                        Math.max(j - 1, 0), Math.min(j + 1, am[i].length - 1)
                )
                        .filter(aj -> am[ai][aj].gas >= 0 ||  Math.abs(am[ai][aj].steering) <= 0.05 || (Math.abs(am[ai][aj].steering) <= 0.1 && am[ai][aj].gas >= -0.2))
                        .mapToObj(aj -> am[ai][aj]))
                        .flatMap(s -> s)
                        .toArray(size -> new Action[size + 1]);
                actions[i][j][actions[i][j].length - 1] = Action.NONE;
            });

        });
    }

    @Override
    public Action[] possibleActions() {
        int i = (int) (prevAction.gas * GAS_FACTOR) + GAS_N;
        int j = (int) (prevAction.steering * STE_FACTOR) + STE_N;
        return actions[i][j];
    }

    @Override
    public void applyAction(Action action) {
        this.prevAction = action;
        LineSegment out = ForwardModel.forwardStep(motion(), action);
        position = out.p;
        velocity = out.v;
        sensor = vs.transform(origin, this);
        depth++;
    }

    @Override
    public boolean isTerminal() {
        if (sensor == null) {
            sensor = vs.transform(origin, this);
        }
        return Math.abs(sensor.getTrackPosition()) > OFF_COURSE;
    }

    @Override
    public boolean isSimulationTerminal() {
        if (sensor == null) {
            sensor = vs.transform(origin, this);
        }
        return Math.abs(sensor.getTrackPosition()) > OFF_TRACK;
    }

    @Override
    public double value() {
        if (sensor == null) {
            sensor = vs.transform(origin, this);
        }
        double time = depth * TIME_STEP;
        // Equation of motion.
        // http://www.wolframalpha.com/input/?i=equation+of+motion+acceleration
        double dmax = 0.5 * (ACC_MAX * time * time) + oldVelocity.size() * time;
        double dist = sensor.getDistanceFromStartLine() - origin;
        if (sensor.newLap() && sensor.getDistanceFromStartLine() < origin) {
            dist += vs.trackLength();
        }
        double value = dist / dmax;
        return value * (isSimulationTerminal() ? value * PENALTY_FACTOR : 1);
    }

    @Override
    public Action defaultAction() {
        return Action.NONE;
    }
}
