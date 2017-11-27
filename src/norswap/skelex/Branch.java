package norswap.skelex;

import norswap.skelex.regex.Choice;
import java.util.Objects;

/**
 * A part of a {@link MatchTree} corresponding to a {@link Choice}.
 */
public final class Branch
{
    /**
     * Index of the selected choice alternative.
     */
    public final int index;

    /**
     * The match value for the selected alternative.
     */
    public final Object value;

    public Branch (int index, Object value)
    {
        this.index = index;
        this.value = value;
    }

    @Override public int hashCode()
    {
        return index * 31 + value.hashCode();
    }

    @Override public boolean equals (Object other)
    {
        if (!(other instanceof Branch)) return false;
        Branch o = (Branch) other;
        return o.index == index
            && Objects.deepEquals(o.value, value);
    }

    @Override public String toString()
    {
        return "(" + index + ", " + value + ")";
    }
}
