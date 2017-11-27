package norswap.skelex.dsl;

import java.util.function.Predicate;

/**
 * A predicate that succeeds if its argument equals a pre-determined string.
 */
public final class StringPredicate implements Predicate<Object>
{
    private final String string;

    public StringPredicate (String string) {
        this.string = string;
    }

    @Override public boolean test (Object o) {
        return o.equals(string);
    }

    @Override public String toString() {
        return "str(" + string + ")";
    }
}
