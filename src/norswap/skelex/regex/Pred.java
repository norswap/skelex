package norswap.skelex.regex;

import norswap.skelex.Regex;
import norswap.skelex.DSL;
import norswap.utils.Predicates;
import java.util.function.Predicate;

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
        String pred_str =  Predicates.to_string(pred);
        return pred_str.equals("pred")
            ? pred_str
            : "pred(" + pred_str + ")";
    }
}
