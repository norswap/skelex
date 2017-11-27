package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.DSL;
import java.util.Arrays;

/**
 * A regex that matches one of multiple regexes.
 */
public final class Choice extends Regex
{
    /**
     * The sub-regexes matched by this regex. Do not mutate.
     */
    public final Regex[] items;

    /**
     * Creates a choice with the given regexes.
     * <p>
     * Use {@link DSL#seq(Object...)} or {@link DSL#seq()} in preference.
     */
    public Choice (Regex... items) {
        this.items = items;
    }

    @Override public String toString()
    {
        return "choice" + Arrays.toString(items);
    }
}