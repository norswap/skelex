package norswap.skelex;

import norswap.skelex.regex.*;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static norswap.utils.Predicates.TRUE;

/**
 * This internal class contains the logic that compiles a {@link Regex} into an {@link Automaton}.
 * <p>
 * The (private) entry point for this functionality is {@link Regex#automaton())}.
 * <p>
 * The builder methods are annotated with diagrams representing the automaton being built.
 * These diagrams are also available in {@code doc/diagrams.md}. This document also explains
 * the conventions used in the automatons.
 */
final class AutomatonBuilder
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Dispatches the regex to the appropriate compile method.
     */
    static Automaton build_automaton (Regex regex)
    {
        /**/ if (regex instanceof Seq)      return build_automaton((Seq) regex);
        else if (regex instanceof Choice)   return build_automaton((Choice) regex);
        else if (regex instanceof Maybe)    return build_automaton((Maybe) regex);
        else if (regex instanceof ZeroMore) return build_automaton((ZeroMore) regex);
        else if (regex instanceof OneMore)  return build_automaton((OneMore) regex);
        else if (regex instanceof Pred)     return build_automaton((Pred) regex);
        else if (regex instanceof Typed)    return build_automaton((Typed) regex);

        throw new IllegalArgumentException();
    }

    // ---------------------------------------------------------------------------------------------

    private static Automaton concat (Automaton a1, Automaton a2)
    {
        a1.end.transitions.addAll(a2.start.transitions);
        return new Automaton(null, a1.start, a2.end);
    }

    // ---------------------------------------------------------------------------------------------

    private static final BiConsumer<MatchTree, Object> NOOP = (m, o) -> {};

    // ---------------------------------------------------------------------------------------------

    private static final BiConsumer<MatchTree, Object> ACCRETE = (m, o) -> m.accrete();

    // ---------------------------------------------------------------------------------------------

    private static void normal_transition (State src, State dst, Predicate<Object> pred)
    {
        src.transitions.add(new Transition(dst, pred, MatchTree::push, Transition.NORMAL));
    }

    // ---------------------------------------------------------------------------------------------

    private static void pre_transition
            (State src, State dst, BiConsumer<MatchTree, Object> action)
    {
        src.transitions.add(new Transition(dst, TRUE, action, Transition.PRE));
    }

    // ---------------------------------------------------------------------------------------------

    private static void post_transition
            (State src, State dst, BiConsumer<MatchTree, Object> action)
    {
        src.transitions.add(new Transition(dst, TRUE, action, Transition.POST));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * <pre>
     * +---+  1  +----+     +----+     +----+  2  +---+
     * | s |-->--| As |==>==| Ae |==>==| Be |-->--| e |
     * +---+     +----+     +----+     +----+     +---+
     *
     * 1: PRE  - mark
     * 2: POST - collect
     * </pre>
     */
    private static Automaton build_automaton (Seq regex)
    {
        Regex[] items = regex.items;
        assert items.length > 0;

        Automaton sub = build_automaton(items[0]);
        for (int i = 1; i < items.length; ++i)
            sub = concat(sub, build_automaton(items[i]));

        State start = new State();
        State end   = new State();

        pre_transition  (start,   sub.start,  (m, o) -> m.mark());
        post_transition (sub.end, end,        (m, o) -> m.collect());

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * <pre>
     *              1  +----+     +----+  3
     *           +-->--| As |==>==| Ae |-->--+
     * +---+     |     +----+     +----+     |     +---+
     * | s |-->--+                           +-->--| e |
     * +---+     |  2  +----+     +----+  4  |     +---+
     *           +-->--| Bs |==>==| Be |-->--+
     *                 +----+     +----+
     *
     *  1, 2: PRE  - noop
     *  3, 4: POST - push_branch(0/1)
     * </pre>
     */
    private static Automaton build_automaton (Choice regex)
    {
        Regex[] items = regex.items;
        assert items.length > 0;

        State start = new State();
        State end   = new State();

        for (int i = 0; i < items.length; ++i)
        {
            Automaton sub = build_automaton(items[i]);
            int index = i;
            pre_transition  (start,   sub.start, NOOP);
            post_transition (sub.end, end,       (m, o) -> m.push_branch(index));
        }

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * <pre>
     * +---+    1    +---+    2    +---+
     * | s |---->----| m |---->----| e |
     * +---+         +---+         +---+
     *   |                           |
     *   |  3  +-+--+     +----+  4  |
     *   +-->--| As |==>==| Ae |-->--+
     *         +-+--+     +----+
     *
     * 1: PRE  - push null
     * 2: POST - noop
     * 3: PRE  - noop
     * 4: POST - noop
     * </pre>
     */
    private static Automaton build_automaton (Maybe regex)
    {
        State     start = new State();
        State     mid   = new State();
        State     end   = new State();
        Automaton sub   = build_automaton(regex.item);

        pre_transition  (start,   mid,       (m, o) -> m.push(null));
        post_transition (mid,     end,       NOOP);
        pre_transition  (start,   sub.start, NOOP);
        post_transition (sub.end, end,       NOOP);

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * <pre>
     *  +---+  1  +-----+  2  +----+  3  +----+
     *  | s +-->--|  m  |-->--| e1 |-->--| e2 |
     *  +---+     +-+-+-+     +----+     +----+
     *         4    | |    5
     * +-------<----+ +----<-------+
     * |                           |
     * |     +-+--+     +----+     |
     * +-->--| As |==>==| Ae |-->--+
     *       +-+--+     +----+
     *
     * 1: PRE  - push list
     * 2: PRE  - noop
     * 3: POST - noop
     * 4: PRE  - noop
     * 5: POST - accrete
     * </pre>
     */
    private static Automaton build_automaton (ZeroMore regex)
    {
        State     start = new State();
        State     mid   = new State();
        State     end1   = new State();
        State     end2   = new State();
        Automaton sub   = build_automaton(regex.item);

        pre_transition  (start,   mid,       (m, o) -> m.push(new ArrayList<>()));
        pre_transition  (mid,     sub.start, NOOP);
        post_transition (sub.end, mid,       ACCRETE);
        pre_transition  (mid,     end1,      NOOP);
        post_transition (end1,    end2,      NOOP);

        return new Automaton(regex, start, end2);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * <pre>
     * +---+  1  +-+--+     +----+  2  +---+
     * | s |-->--| As |==>==| Ae |-->--| e |
     * +---+     +-+--+     +--+-+     +---+
     *             |     3     |
     *             +-----<-----+
     *
     * 1: PRE  - push list
     * 2: POST - accrete
     * 3: POST - accrete
     * </pre>
     */
    private static Automaton build_automaton (OneMore regex)
    {
        State     start = new State();
        State     end   = new State();
        Automaton sub   = build_automaton(regex.item);

        pre_transition  (start,   sub.start, (m, o) -> m.push(new ArrayList<>()));
        post_transition (sub.end, sub.start, ACCRETE);
        post_transition (sub.end, end,       ACCRETE);

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    private static Automaton build_automaton (Pred regex)
    {
        State start = new State();
        State end   = new State();

        normal_transition(start, end, regex.pred);

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    private static Automaton build_automaton (Typed regex)
    {
        State start = new State();
        State end   = new State();

        normal_transition(start, end, it -> regex.type.isInstance(it) && regex.pred.test(it));

        return new Automaton(regex, start, end);
    }

    // ---------------------------------------------------------------------------------------------
}
