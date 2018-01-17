package norswap.skelex;

import norswap.skelex.regex.*;
import norswap.skelex.dsl.*;
import java.util.function.Predicate;

import static norswap.skelex.dsl.Conversions.*;
import static norswap.utils.Predicates.TRUE;
import static norswap.utils.Util.cast;

/**
 * Use `{@code import static norswap.skelex.DSL.*}` to enable DSL construction of {@link Regex}
 * objects.
 * <p>
 * This class provides two kinds of DSL:
 * <p>
 * First, a simple nesting-based DSL that essentially removes the need for constructor calls and
 * that pesky "new" keyword.
 * <p>
 * Second, a builder-style DSL where you can chain method calls to create your regex. The methods
 * you can use for this are {@link #builder(Object)}, {@link #choice()}, {@link #seq()}. Also check
 * the classes {@link Builder}, {@link ChoiceBuilder} and {@link SeqBuilder}.
 * <p>
 * Both types of DSLs can be mixed.
 */
public final class DSL
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Typed} regex without predicate.
     */
    public static Typed typed (Class<?> klass) {
        return new Typed(klass);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Typed} regex.
     */
    public static <T> Typed typed (Class<T> klass, Predicate<T> pred) {
        return new Typed(klass, cast(pred));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a new {@link Pred} regex that matches if the input item equals {@code string}.
     */
    public static Pred string (String string) {
        return new Pred(new StringPredicate(string));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Pred} regex.
     */
    public static Pred pred (Predicate<Object> pred) {
        return new Pred(pred);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Seq} regex, converting the passed objects into regexes
     * via {@link Conversions#regex}.
     */
    public static Seq seq (Object... objs) {
        return new Seq(regexes(objs));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Choice} regex, converting the passed objects into regexes
     * via {@link Conversions#regex}.
     */
    public static Choice choice (Object... objs) {
        return new Choice(regexes(objs));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Maybe} regex, converting the passed object into a regex
     * via {@link Conversions#regex}.
     */
    public static Maybe maybe (Object obj) {
        return new Maybe(regex(obj));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link Maybe} regex, wrapping a sequence of regexes obtained by converting the
     * items in {@code objs} via {@link Conversions#regex}.
     */
    public static Maybe maybe (Object... objs) {
        return new Maybe(seq(objs));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link ZeroMore} regex, converting the passed object into a regex
     * via {@link Conversions#regex}.
     */
    public static ZeroMore zeromore (Object obj) {
        return new ZeroMore(regex(obj));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link ZeroMore} regex, wrapping a sequence of regexes obtained by converting the
     * items in {@code objs} via {@link Conversions#regex}.
     */
    public static ZeroMore zeromore (Object... objs) {
        return new ZeroMore(seq(objs));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link OneMore} regex, converting the passed object into a regex
     * via {@link Conversions#regex}.
     */
    public static OneMore onemore (Object obj) {
        return new OneMore(regex(obj));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Constructs a {@link OneMore} regex, wrapping a sequence of regexes obtained by converting the
     * items in {@code objs} via {@link Conversions#regex}.
     */
    public static OneMore onemore (Object... objs) {
        return new OneMore(seq(objs));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates an empty {@link ChoiceBuilder}.
     */
    public static ChoiceBuilder choice() {
        return new ChoiceBuilder();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates an empty {@link SeqBuilder}.
     */
    public static SeqBuilder seq() {
        return new SeqBuilder();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a {@link Builder} wrapping a regex, obtained by converting {@code obj}
     * via {@link Conversions#regex}.
     */
    public static Builder builder (Object obj) {
        return new ContainerBuilder(regex(obj));
    }

    // ---------------------------------------------------------------------------------------------
}
