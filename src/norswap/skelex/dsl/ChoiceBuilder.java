package norswap.skelex.dsl;

import norswap.skelex.regex.Choice;
import norswap.skelex.Regex;
import java.util.ArrayList;

/**
 * Fluent API builder for a {@link Choice} regex.
 */
public final class ChoiceBuilder extends Builder
{
    // ---------------------------------------------------------------------------------------------

    private ArrayList<Regex> regexes = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a new regex alternative to the choice being built, by converting {@code obj}
     * via {@link Conversions#regex}.
     */
    public ChoiceBuilder or (Object obj)
    {
        regexes.add(Conversions.regex(obj));
        return this;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Choice end() {
        return new Choice(regexes.toArray(new Regex[0]));
    }

    // ---------------------------------------------------------------------------------------------
}
