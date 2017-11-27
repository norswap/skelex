package norswap.skelex.dsl;

import norswap.skelex.regex.Seq;
import norswap.skelex.Regex;
import java.util.ArrayList;

/**
 * Fluent API builder for a {@link Seq} regex.
 */
public final class SeqBuilder extends Builder
{
    // ---------------------------------------------------------------------------------------------

    private ArrayList<Regex> regexes = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a new regex to the sequence being built, by converting {@code obj}
     * via {@link Conversions#regex}.
     */
    public SeqBuilder then (Object obj)
    {
        regexes.add(Conversions.regex(obj));
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Seq end() {
        return new Seq(regexes.toArray(new Regex[0]));
    }

    // ---------------------------------------------------------------------------------------------
}
