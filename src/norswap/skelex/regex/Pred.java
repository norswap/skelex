package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.DSL;
import java.util.function.Predicate;

import static norswap.utils.Util.attempt;

/**
 * A regex that matches all objects that satisfy a given predicate.
 */
public final class Pred extends Regex
{
    /**
     * The predicate to be satisfied for this regex to match.
     */
    public final Predicate<Object> pred;

    /**
     * Creates a {@link Pred} regex with the given predicate.
     * Use {@link DSL#pred(Predicate)})} in preference.
     */
    public Pred (Predicate<Object> pred) {
        this.pred = pred;
    }

    @Override public String toString()
    {
        Class<?> pred_tostring_src
            = attempt(() -> pred.getClass().getMethod("toString").getDeclaringClass());

        boolean overriden
            = pred != null && !pred_tostring_src.equals(Object.class);

        return overriden
            ? pred.toString()
            : "pred";
    }
}
