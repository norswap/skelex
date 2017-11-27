package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.dsl.Builder;
import norswap.skelex.DSL;

/**
 * A regex that matches one or more repetition of another regex.
 */
public final class OneMore extends Regex
{
    /**
     * The sub-regex repeatedly matched by this regex.
     */
    public final Regex item;

    /**
     * Creates a {@link OneMore} regex with the given item.
     * <p>
     * Use {@link DSL#onemore(Object)} or {@link Builder#onemore()} in preference.
     */
    public OneMore (Regex item) {
        this.item = item;
    }

    @Override public String toString()
    {
        return item + "+";
    }
}
