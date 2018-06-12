package maig.mcts;

import static maig.Constants.MCTS_C;
import static maig.Constants.SIM_DEPTH;
import scr.maig.Debug.DebugDraw;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Jacob Fischer (jaco@itu.dk)
 * @param <A> the type of the actions that the MCTS should search through
 * @param <S> the type representing the game state in the MCTS
 */
public class Node<A, S extends State<A>> {

    private final A action;
    private final State<A> state;
    private final ArrayList<A> actions;
    private final ArrayList<Node<A, S>> children = new ArrayList<>();
    private final Random rng;
    private int visits = 0;
    private double value = 0.0;

    public Node(S state) {
        this.state = state;
        action = state.defaultAction();
        actions = new ArrayList<>(Arrays.asList(state.possibleActions()));
        rng = new Random();
    }

    private Node(Node<A, S> parent, A action) {
        this.action = action;
        state = parent.state.copy();
        state.applyAction(action);
        actions = new ArrayList<>(Arrays.asList(state.possibleActions()));
        rng = new Random();
    }

    public void run(double dueTime) {
        double start, avg = 0;
        // Keep iterating until the time budget is nearly up.
        for (int i = 0; System.currentTimeMillis() < (dueTime - 2 * avg); i++) {
            start = System.currentTimeMillis();
            ArrayList<Node> visited = new ArrayList<>();
            visited.add(this);
            Node current = this;
            // Selection.
            while (!current.isTerminal() && !current.isLeaf()) {
                current = current.selection();
                visited.add(current);
            }
            // Expansion.
            if (current.isLeaf() && !current.isTerminal()) {
                current = current.expand();
                visited.add(current);
            }
            // Simulation.
            double v = current.simulation();
            // Backpropagation.
            visited.stream().forEach(n -> n.update(v));
            avg = (avg + System.currentTimeMillis() - start) / (i + 1);
        }
    }

    public A getDecision() {
        return best().action;
    }

    public ArrayList<A> getDecisionPath() {
        Node<A, S> node = this;
        ArrayList<A> path = new ArrayList<>();
        while (!node.children.isEmpty()) {
            node = node.best();
            path.add(node.action);
        }
        return path;
    }

    public double getBestChildValue() {
        Node<A, S> n = best();
        return n.value / n.visits;
    }

    // The child node with the highest value.
    private Node<A, S> best() {
        return children.stream().filter(c -> !c.isTerminal()).max((o1, o2)
                -> (int) Math.signum(o1.value - o2.value)).orElse(this);
    }

    // The child node with the most visits.
    private Node<A, S> safest() {
        return children.stream().filter(c -> !c.isTerminal()).max((o1, o2)
                -> o1.visits - o2.visits).orElse(this);
    }

    private Node selection() {
        return children.stream().max((o1, o2)
                -> (int) Math.signum(UCTValue(o1) - UCTValue(o2))).get();
    }

    private Node expand() {
        A a = actions.get(rng.nextInt(actions.size()));
        actions.remove(a);
        Node<A, S> c = new Node<>(this, a);
        children.add(c);

        if(!c.state.isSimulationTerminal())
            DebugDraw.addDebugBluePoint(((maig.model.mcts.State) c.state).position().x, ((maig.model.mcts.State) c.state).position().y);
        return c;
    }

    private double simulation() {
        State<A> s = state.copy();
        for (int i = 0; i < SIM_DEPTH && !s.isSimulationTerminal(); i++) {
            A[] a = s.possibleActions();
            s.applyAction(a[rng.nextInt(a.length)]);
        }
        return s.value();
    }

    private void update(double value) {
        visits++;
        this.value += value;
    }

    private double UCTValue(Node node) {
        double tiebreak = rng.nextDouble() * 1e-9;
        double w = node.value;
        int n = node.visits;
        int t = visits;
        return (w / n) + MCTS_C * Math.sqrt((2 * Math.log(t)) / n) + tiebreak;
    }

    private boolean isLeaf() {
        return !actions.isEmpty();
    }

    private boolean isTerminal() {
        return state.isTerminal();
    }
}
