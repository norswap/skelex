package norswap.skelex.dsl;

import norswap.skelex.Regex;

/**
 * Fluent API builder that wraps a regex and enables calling the methods defined
 * in {@link Builder} on it.
 */
public final class ContainerBuilder extends Builder
{
    // ---------------------------------------------------------------------------------------------

    public final Regex regex;

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a new builder that wraps a regex, obtained by converting {@code obj}
     * via {@link Conversions#regex}.
     */
    public ContainerBuilder (Object obj) {
        this.regex = Conversions.regex(obj);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Regex end () {
        return regex;
    }

    // ---------------------------------------------------------------------------------------------
}
