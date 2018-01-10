package norswap.skelex;

import norswap.skelex.regex.*;
import norswap.utils.ArrayStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static norswap.utils.Util.cast;

/**
 * A {@link Match} object that also records the structure of an input string matched
 * by a regular expression (a parse tree).
 * <p>
 * This object encapsulates a composite tree structure made of:
 * <ul>
 *     <li>lists: matching the sub-matches in sequences and repetitions
 *         ({@link Seq}, {@link ZeroMore}, {@link OneMore})</li>
 *     <li>{@link Branch} objects: matching a particular {@link Choice} alternative</li>
 *     <li>{@code null} values: matching an empty {@link Maybe} regex</li>
 *     <li>other objects, corresponding to the matched input items</li>
 * </ul><p>
 * Parts of this structure can be accessed through the various methods exposed by this class.
 */
public final class MatchTree extends Match
{
    // =============================================================================================

    /**
     * A wildcard that can be used within chained index functions {@link #get(Object, int...)}.
     * <p>
     * Whenever the wildcard is used instead of a regular index, the result of the access is a list
     * where each item is the result of the rest of the access chain.
     * <p>
     * For instance, {@code tree.get($, 2)} returns a list of items at index 2 within the sub-lists
     * held within the root list. It's equivalent to the pseudo-code {@code tree.list().foreach {
     * get(it, 2) }}.
     */
    public static final int $ = -42;

    // =============================================================================================

    private static final Object MARKER = new Object();

    // ---------------------------------------------------------------------------------------------

    private ArrayStack<Object> stack = new ArrayStack<>();

    // ---------------------------------------------------------------------------------------------

    MatchTree (Regex regex, int start, int end)
    {
        super(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pushes an item to the top of the stack.
     */
    void push (Object item) {
        stack.push(item);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pushes a marker on the top of the stack.
     */
    void mark() {
        push(MARKER);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Gathers all items on the stack until a marker is encountered, put them inside an array list,
     * then push the list on the stack (the items and the marker having been removed).
     */
    void collect()
    {
        ArrayList<Object> out = new ArrayList<>();
        Object top = stack.pop();
        while (top != MARKER) {
            out.add(top);
            top = stack.pop();
        }
        Collections.reverse(out);
        stack.push(out);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Commit the outcome of the last choice by wrapping the item at the top of the stack with a
     * {@link Branch} that includes the given index.
     */
    void push_branch (int index)
    {
        stack.push(new Branch(index, stack.pop()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Accrete a repetition choice: pops the top item of the stack, and adds it to the second item,
     * which should be an array list.
     */
    void accrete()
    {
        Object top = stack.pop();
        ArrayList<Object> list = cast(stack.peek());
        list.add(top);
    }

    // =============================================================================================
    // ROOT ACCESS

    /**
     * Returns the root of the matched tree.
     */
    public Object $value() {
        return stack.peek();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the root of the matched tree, attempting to cast it to the expected type.
     */
    public <T> T value() {
        return cast(stack.peek());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the root of the matched tree, attempting to cast it to a list.
     */
    public List<?> list() {
        return cast(stack.peek());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the root of the matched tree, attempting to cast it to a {@link Branch}.
     */
    public Branch branch() {
        return cast(stack.peek());
    }

    // =============================================================================================
    // STATIC CHAIN ACCESS

    /**
     * Returns the object obtained by casting {@code obj} into a list, then getting the item
     * at the first index in {@code indices}, then repeatedly applying this process on the last
     * obtained item until all indices have been exhausted.
     * <p>
     * If at some point an item cannot be cast into a list or a list is too short for the given
     * index, an exception is thrown.
     */
    public static Object $get (Object obj, int... indices)
    {
        int i = 0;
        while (i < indices.length)
        {
            int index = indices[i];

            if (obj instanceof Branch)
            {
                obj = ((Branch) obj).value;
                continue;
            }
            else if (obj instanceof List)
            {
                List<?> list = (List<?>) obj;

                if (index == $) {
                    ArrayList<Object> out = new ArrayList<>(list.size());
                    for (Object o: list)
                        out.add($get(o, Arrays.copyOfRange(indices, i + 1, indices.length)));
                    return out;
                }

                obj = list.get(index);
            }
            else
                throw new IllegalArgumentException(i + "th item in the path cannot be indexed");

            ++i;
        }
        return obj;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Like {@link #$get}, but attempting to cast the result to the required type.
     */
    public static <T> T get(Object obj, int... indices) {
        return cast($get(obj, indices));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the object obtained by casting {@code obj} into a list, then getting the item
     * at the first index in {@code indices}, then repeatedly applying this process on the last
     * obtained item until all indices have been exhausted.
     * <p>
     * If at some point the function attempts to index a null value, or if a list is too short
     * for the given index, null is returned. This is how this function differs from {@link #$get}.
     * <p>
     * If at some point an item cannot be cast into a list, an exception is thrown.
     */
    public static Object $fmap (Object obj, int... indices)
    {
        int i = 0;
        while (i < indices.length)
        {
            int index = indices[i];

            if (obj instanceof Branch) {
                obj = ((Branch) obj).value;
                continue;
            }
            else if (obj instanceof List)
            {
                List<?> list = ((List<?>) obj);

                if (index == $) {
                    ArrayList<Object> out = new ArrayList<>(list.size());
                    for (Object o: list)
                        out.add($fmap(o, Arrays.copyOfRange(indices, i + 1, indices.length)));
                    return out;
                }

                if (list.size() <= index) // ADDITION to $get() code
                    return null;

                obj = list.get(index);
            }
            else if (obj == null) // ADDITION to $get() code
                return null;
            else
                throw new IllegalArgumentException("item " + i + " in the path cannot be indexed");

            ++i;
        }
        return obj;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Like {@link #$fmap}, but attempting to cast the result to the required type.
     */
    public static <T> T fmap (Object obj, int... indices) {
        return cast($fmap(obj, indices));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #$get} but attempts to cast the result to a list.
     */
    public static List<?> get_list (Object obj, int... indices) {
        return cast($get(obj, indices));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #$fmap}, but attempts to cast the result to a list.
     */
    public static List<?> fmap_list (Object obj, int... indices) {
        return cast($fmap(obj, indices));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #$get}, but attempts to cast the result to a {@link Branch}.
     */
    public static Branch get_branch (Object obj, int... indices) {
        return cast($get(obj, indices));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #$get}, but attempts to cast the result to a {@link Branch}.
     */
    public static Branch fmap_branch (Object obj, int... indices) {
        return cast($fmap(obj, indices));
    }

    // =============================================================================================
    // CHAIN ACCESS FROM ROOT

    /**
     * Same as {@link #$get(Object, int...)}, using the root of the tree as first parameter.
     */
    public Object $get (int... indices) {
        return $get(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #get(Object, int...)}, using the root of the tree as first parameter.
     */
    public <T> T get (int... indices) {
        return get(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #$fmap(Object, int...)}, using the root of the tree as first parameter.
     */
    public Object $fmap (int... indices) {
        return $fmap(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #fmap(Object, int...)}, using the root of the tree as first parameter.
     */
    public <T> T fmap (int... indices) {
        return fmap(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #get_list(Object, int...)}, using the root of the tree as first parameter.
     */
    public List<?> get_list (int... indices) {
        return get_list(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #fmap_list(Object, int...)}, using the root of the tree as first parameter.
     */
    public List<?> fmap_list (int... indices) {
        return fmap_list(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #get_branch(Object, int...)}, using the root of the tree as first parameter.
     */
    public Branch get_branch (int... indices) {
        return get_branch(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #fmap_branch(Object, int...)}, using the root of the tree as first parameter.
     */
    public Branch fmap_branch (int... indices) {
        return fmap_branch(this, indices);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return super.toString() + "<" + Objects.toString(stack.peek()) + ">";
    }

    // ---------------------------------------------------------------------------------------------
}
