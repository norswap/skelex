package norswap.skelex;

import norswap.utils.Pair;
import java.util.ArrayList;

/**
 * Checkpoints are used by the {@link Runner} to record reached states ({@link State}), as well as
 * the path used to reach them (as reverse linked lists of checkpoints via {@link #transitions}).
 */
final class Checkpoint
{
    // ---------------------------------------------------------------------------------------------

    final State state;

    // ---------------------------------------------------------------------------------------------

    /**
     * The regex being matched.
     */
    final Regex regex;

    // ---------------------------------------------------------------------------------------------

    /**
     * Input position at which the match was started.
     */
    final int start;

    // ---------------------------------------------------------------------------------------------

    /**
     * Position at which the checkpoint is stored if it holds an anchor state;
     * or position of the next checkpoint with an anchor state that (transitively) links to this.
     */
    final int pos;

    // ---------------------------------------------------------------------------------------------

    /**
     * Records the {@link Transition} taken to reach this checkpoint as well as the checkpoint
     * containing the state each of these transitions came from.
     */
    final ArrayList<Pair<Checkpoint, Transition>> transitions = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    Checkpoint (State state, int start, int pos, Regex regex)
    {
        this.state = state;
        this.start = start;
        this.pos = pos;
        this.regex = regex;
    }

    // ---------------------------------------------------------------------------------------------

    void add_transition (Checkpoint source, Transition transition) {
        transitions.add(new Pair<>(source, transition));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A state is accepting (it is the end state of its automaton) if has no outgoing transitions.
     */
    boolean accepting() {
        return state.transitions.isEmpty();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int hashCode() {
        return 31 * 31 * state.hashCode() + 31 * regex.hashCode() + start;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean equals (Object other)
    {
        if (!(other instanceof Checkpoint)) return false;
        Checkpoint o = (Checkpoint) other;
        return state == o.state && regex == o.regex && start == o.start;
    }

    // ---------------------------------------------------------------------------------------------
}
