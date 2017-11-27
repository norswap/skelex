package norswap.skelex;

import java.util.ArrayList;

/**
 * A state is a constituent part of an {@link Automaton} and can have multiple incoming or
 * outgoing {@link Transition}s. Only the outgoing transitions are recorded in this class.
 */
final class State
{
    final ArrayList<Transition> transitions = new ArrayList<>();

    void add (Transition transition) {
        transitions.add(transition);
    }
}
