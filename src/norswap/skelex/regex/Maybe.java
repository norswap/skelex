package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.dsl.Builder;
import norswap.skelex.DSL;

/**
 * A regex that matches another regex or succeeds without matching anything.
 */
public final class Maybe extends Regex
{
    /**
     * The sub-regex potentially matched by this regex.
     */
    public final Regex item;

    /**
     * Creates a {@link Maybe} regex with the given item.
     * <p>
     * Use {@link DSL#maybe(Object)} or {@link Builder#maybe()} in preference.
     */
    public Maybe (Regex item) {
        this.item = item;
    }

    @Override public String toString() {
        return item + "?";
    }
}
