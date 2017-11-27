package norswap.skelex;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A transition enables going from one {@link State} to another in an {@link Automaton}.
 * Each state may have multiple incoming or outgoing transitions.
 * <p>
 * Each transition records its target state. as well as a predicate it needs to satisfy in order to
 * be taken, and an action to be used to build up the automaton's {@link MatchTree} object.
 * <p>
 * There are three kinds of transitions (NORMAL, PRE, POST). For more information, refer to the
 * {@code doc/implementation.md} document in the source tree.
 */
final class Transition
{
    static final int PRE    = 1;
    static final int POST   = 2;
    static final int NORMAL = 3;

    final State target;
    final Predicate<Object> predicate;
    final BiConsumer<MatchTree, Object> action;
    final int type;

    Transition (State target, Predicate<Object> predicate, BiConsumer<MatchTree, Object> action, int type)
    {
        this.target    = target;
        this.predicate = predicate;
        this.action    = action;
        this.type      = type;
    }
}
