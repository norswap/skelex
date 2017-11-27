package norswap.skelex.dsl;

import norswap.skelex.Regex;
import norswap.skelex.regex.*;

/**
 * Abstract parent class for all regex builders.
 * <p>
 * Used as a fluent API (builder-style) DSL for regex construction.
 */
public abstract class Builder
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the regex built by this builder.
     */
    public abstract Regex end();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a builder for a {@link Maybe} regex applied on the regex built by this builder.
     */
    public ContainerBuilder maybe() {
        return new ContainerBuilder(new Maybe(end()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a builder for a {@link ZeroMore} regex applied on the regex built by this builder.
     */
    public ContainerBuilder zeromore() {
        return new ContainerBuilder(new ZeroMore(end()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a builder for a {@link OneMore} regex applied on the regex built by this builder.
     */
    public ContainerBuilder onemore() {
        return new ContainerBuilder(new OneMore(end()));
    }

    // ---------------------------------------------------------------------------------------------
}
