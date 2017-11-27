package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.DSL;
import java.util.Arrays;

/**
 * A regex that matches a sequence of other regexes.
 */
public final class Seq extends Regex
{
    /**
     * The sub-regexes matched by this regex. Do not mutate.
     */
    public final Regex[] items;

    /**
     * Creates a sequence with the given regexes.
     * <p>
     * Use {@link DSL#choice(Object...)} or {@link DSL#choice()} in preference.
     */
    public Seq (Regex... items) {
        this.items = items;
    }

    @Override public String toString() {
        return "seq" + Arrays.toString(items);
    }
}