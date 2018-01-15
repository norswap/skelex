package norswap.skelex;

import norswap.utils.Arrays;

/**
 * Checkpoints are used by the {@link Runner} to record reached states ({@link State}), as well as
 * the path used to reach them (as reverse linked lists of checkpoints).
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
     * Whethever the checkpoint should be considered when feeding input to the runner.
     */
    boolean live = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Number of incoming transitions.
     */
    private int transition_count = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Container for {@link #transition_count} incoming transitions.
     */
    private Transition[] transitions = new Transition[1];

    // ---------------------------------------------------------------------------------------------

    /**
     * Holds the source checkpoints for the correspond transitions in {@link #transitions}.
     */
    private Checkpoint[] transources = new Checkpoint[1];

    // ---------------------------------------------------------------------------------------------

    Checkpoint (State state, int start, int pos, Regex regex)
    {
        this.state = state;
        this.start = start;
        this.pos = pos;
        this.regex = regex;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the number of incoming transitions.
     */
    int transition_count() {
        return transition_count;
    }

    // ---------------------------------------------------------------------------------------------

    Transition transition (int index) {
        return transitions[index];
    }

    // ---------------------------------------------------------------------------------------------

    Checkpoint transition_source (int index) {
        return transources[index];
    }

    // ---------------------------------------------------------------------------------------------

    void add_transition (Checkpoint source, Transition transition)
    {
        if (transition_count == transitions.length) {
            transitions = Arrays.resize_binary_power(transitions, transition_count + 1);
            transources = Arrays.resize_binary_power(transources, transition_count + 1);
        }

        transitions[transition_count] = transition;
        transources[transition_count] = source;
        ++ transition_count;
    }

    // ---------------------------------------------------------------------------------------------

    void merge_transitions (Checkpoint other)
    {
        int new_count = transition_count + other.transition_count;

        if (new_count > transitions.length) {
            transitions = Arrays.resize_binary_power(transitions, new_count);
            transources = Arrays.resize_binary_power(transources, new_count);
        }

        System.arraycopy(
            other.transitions, 0,
            transitions, transition_count,
            other.transition_count);

        System.arraycopy(
            other.transources, 0,
            transources, transition_count,
            other.transition_count);

        transition_count = new_count;
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
