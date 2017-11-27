package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.dsl.Builder;
import norswap.skelex.DSL;

/**
 * A regex that matches zero or more repetition of another regex.
 */
public final class ZeroMore extends Regex
{
    /**
     * The sub-regex repeatedly matched by this regex.
     */
    public final Regex item;

    /**
     * Creates a {@link ZeroMore} regex with the given item.
     * Use {@link DSL#zeromore(Object)} or {@link Builder#zeromore()} in preference.
     */
    public ZeroMore (Regex item) {
        this.item = item;
    }

    @Override public String toString()
    {
        return item + "*";
    }
}
