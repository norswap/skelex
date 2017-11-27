package norswap.skelex;

/**
 * A match object represent a match of {@link #regex} over a portion of input delimited by
 * the positions {@link #start} and {@link #end}.
 * <p>
 * Match objects can be acquired through a {@link MatchStream}.
 * <p>
 * If you need to see how the input matches the structure of the regex (i.e. acquire a parse tree),
 * request a {@link MatchTree} instead.
 */
public class Match implements Cloneable
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The matched regex.
     */
    public final Regex regex;

    // ---------------------------------------------------------------------------------------------

    /**
     * The input position at which the match started (inclusive).
     */
    public final int start;

    // ---------------------------------------------------------------------------------------------

    /**
     * The input position at which the match ended (exclusive).
     */
    public final int end;

    // ---------------------------------------------------------------------------------------------

    Match (Regex regex, int start, int end)
    {
        this.regex = regex;
        this.start = start;
        this.end = end;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int hashCode() {
        return 31 * 31 * regex.hashCode() + 31 * start + end;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Two matches are equal if they are instances of the same class and are otherwise identical.
     * <p>
     * Subclasses of Match should override this method to uphold this contract. It is recommended
     * to call the super-method in order to establish class equivalence and the equality of the
     * common parts.
     */
    @Override public boolean equals (Object other)
    {
        if (other == null || !getClass().equals(other.getClass())) return false;
        Match o = (Match) other;
        return regex == o.regex && start == o.start && end == o.end;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * {@code "Match[" + start + "-" + end + "](" + regex + ")"}
     */
    @Override public String toString() {
        return "Match[" + start + "-" + end + "](" + regex + ")";
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Match clone() {
        return this;
    }

    // ---------------------------------------------------------------------------------------------
}
