package norswap.skelex.dsl;

import norswap.skelex.Regex;
import norswap.skelex.regex.Pred;
import norswap.skelex.regex.Typed;

import static norswap.utils.Predicates.TRUE;

/**
 * Static methods for converting different types of objects into regexes.
 */
public final class Conversions
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Converts the object into a regex if possible.
     * <p>
     * Currently supported types:
     * <ul>
     *     <li>{@link Regex} - identity</li>
     *     <li>{@link Builder} - using {@link Builder#end}</li>
     *     <li>{@link Class} - converts into a {@link Typed}</li>
     *     <li>{@link String} - converts into a {@link Pred} with a {@link StringPredicate}</li>
     * </ul><p>
     * Throws an exception if the object type isn't supported.
     */
    public static Regex regex (Object obj)
    {
        if (obj instanceof Regex)
            return (Regex) obj;

        if (obj instanceof Builder)
            return ((Builder) obj).end();

        if (obj instanceof Class<?>)
            return new Typed((Class<?>) obj, TRUE);

        if (obj instanceof String)
            return new Pred(new StringPredicate((String) obj));

        throw new IllegalArgumentException(obj + " cannot be converted to a regex.");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Converts each object in the array to a regex, as per {@link #regex(Object)}.
     * May return {@code objs} itself if it is an array of regexes.
     */
    public static Regex[] regexes (Object... objs)
    {
        if (objs instanceof Regex[])
            return (Regex[]) objs;

        Regex[] regexes = new Regex[objs.length];
        for (int i = 0; i < objs.length; ++i)
            regexes[i] = regex(objs[i]);
        return regexes;
    }

    // ---------------------------------------------------------------------------------------------
}
