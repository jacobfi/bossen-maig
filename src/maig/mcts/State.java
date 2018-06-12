package maig.mcts;

/**
 *
 * @author Jacob Fischer (jaco@itu.dk)
 * @param <A> the type of the actions that can lead to states of this type
 */
public interface State<A> {
    public State<A> copy();
    public A[] possibleActions();
    public void applyAction(A action);
    public boolean isTerminal();
    public boolean isSimulationTerminal();
    public double value();
    public A defaultAction();
}
