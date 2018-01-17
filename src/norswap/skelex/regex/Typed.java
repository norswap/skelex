package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.DSL;
import norswap.utils.Predicates;
import java.util.function.Predicate;

import static norswap.utils.Predicates.TRUE;

/**
 * A regex that matches objects of a certain type, that also satisfy a given predicate.
 */
public final class Typed extends Regex
{
    /**
     * The type an object must have to be matched by this regex.
     */
    public final Class<?> type;

    /**
     * The predicate to be satisfied for this regex to match.
     */
    public final Predicate<Object> pred;

    /**
     * Creates a {@link Typed} with the given type and a predicate that always evaluates to true.
     * <p>
     * Prefer using {@link DSL#typed(Class)}.
     */
    public Typed (Class<?> type) {
        this(type, TRUE);
    }

    /**
     * Creates a {@link Typed} regex with the given type and predicate.
     * <p>
     * Prefer using {@link DSL#typed(Class, Predicate)}.
     */
    public Typed (Class<?> type, Predicate<Object> pred) {
        this.type = type;
        this.pred = pred;
    }

    @Override public String toString()
    {
        String pred_str = pred == TRUE ? "" : ("~" + Predicates.to_string(pred));
        return "typed(" + type.getSimpleName() + pred_str + ")";
    }
}
