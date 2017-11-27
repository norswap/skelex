package norswap.skelex;

/**
 * Au automaton is a compiled regex that a {@link Runner} uses to perform matches.
 * <p>
 * An automaton has a start state and an end state.
 * The end state may not have any outgoing transition.
 */
final class Automaton
{
    final State start;
    final State end;

    /**
     * The regex used to build this automaton.
     */
    final Regex regex;

    Automaton (Regex regex, State start, State end)
    {
        this.start  = start;
        this.end    = end;
        this.regex  = regex;
    }
}