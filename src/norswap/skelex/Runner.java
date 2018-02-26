package norswap.skelex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static norswap.skelex.Transition.*;

/**
 * Instances of this class are used to match regexes over an input.
 * <p>
 * Please refer to <a href="https://github.com/norswap/skelex/blob/master/doc/README.md">the user
 * manual</a> for more details, or to <a
 * href="https://github.com/norswap/skelex/blob/master/doc/implementation.md">the implementation
 * guide</a> for implementation details.
 */
public final class Runner
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A value used in place of an input item to signify the absence of input.
     */
    public static final Object NO_INPUT = new Object();

    // ---------------------------------------------------------------------------------------------

    private final CheckpointMap checkpoints = new CheckpointMap();

    // ---------------------------------------------------------------------------------------------

    private ArrayList<Object> input = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    private int pos = 0;

    // =============================================================================================

    /**
     * Adds a new regex to be matched starting at the {@code index} position.
     *
     * @param index a position {@code >=} the current position
     */
    public void add (int index, Regex regex)
    {
        if (index < pos)
            throw new IllegalArgumentException("Can't add an automaton below the current position.");

        add(index, regex.automaton());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a new regex to be matched starting at the current position.
     */
    public void add (Regex regex)
    {
        add(pos, regex.automaton());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Feeds an item of input to the runner, incrementing the current position and potentially
     * furthering the matches of registered regexes.
     */
    public void advance (Object item)
    {
        for (Checkpoint cp: checkpoints.get(pos))
            if (cp.live)
                advance(cp, PRE, item);
        input.add(item);
        ++ pos;
        assert input.size() == pos;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Feeds a collection of items of input (in iteration order) to the runner, increasing the
     * current position and potentially furthering the matches of registered regexes.
     */
    public void advance (Collection<?> items) {
        items.forEach(this::advance);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Feeds a set of input items to the runner, increasing the current position and potentially
     * furthering the matches of registered regexes.
     */
    public void advance (Object... items) {
        for (Object it: items) advance(it);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the current input position.
     */
    public int pos() {
        return pos;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the input seen by the runner.
     */
    public List<?> input() {
        return Collections.unmodifiableList(input);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A return value of true indicates that no previous registration are still able to match
     * more input, in which case advancing in the input without adding new regexes is useless.
     * <p>
     * Note that a return value of false does not guarantee that any matches can still occur!
     */
    public boolean dead() {
        return checkpoints.is_empty(pos);
    }

    // ---------------------------------------------------------------------------------------------

    Stream<Checkpoint> stream (int index) {
        return checkpoints.get(index).stream().filter(Checkpoint::accepting);
    }

    // ---------------------------------------------------------------------------------------------

    Stream<Checkpoint> stream () {
        return stream(pos);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link MatchStream} representing the matches up to position {@code index}.
     */
    public MatchStream matches (int index) {
        return new MatchStream(stream(index), this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link MatchStream} representing the matches up to the current position.
     */
    public MatchStream matches() {
        return matches(pos);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Deletes the {@code amount} last items of input seen by the runner and undo the match
     * progression they caused.
     */
    public void clear_last (int amount)
    {
        if (amount > pos)
            throw new IllegalArgumentException("Trying to clear more input items ("
                + amount + ") than were seen (" + pos + ")");

        input.subList(pos - amount, pos).clear();
        checkpoints.clear_last(pos, amount);
        pos -= amount;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Filter the registrations that will be able to match after the current input position.
     * <p>
     * Feed all registration that are still able to generate new matches after the current input
     * position to {@code pred}, if the result is {@code false}, the registration won't able to
     * match after the current input position.
     * <p>
     * A negative decision can be reversed by another application of this method at the same input
     * position, but NOT by applying this method at subsequent input positions.
     */
    public void filter_registrations (BiPredicate<Regex, Integer> pred)
    {
        for (Checkpoint cp: checkpoints.get(pos))
            cp.live = pred.test(cp.regex, cp.start);
    }

    // =============================================================================================

    /**
     * Adds a new automaton to be matched starting at the {@code index} position.
     *
     * @param index a position {@code >=} the current position
     */
    private void add (int index, Automaton automaton)
    {
        assert index >= pos;

        Checkpoint cp = new Checkpoint(automaton.start, index, index, automaton.regex);
        checkpoints.add(index, cp);

        // pos manipulation necessary because advance(...) adds checkpoints to the next position
        -- pos;
        advance(cp, PRE, NO_INPUT);
        ++ pos;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Attempt to take all valid chains of transitions starting from the state within {@code
     * source}.
     * <p>
     * The next transition that can be taken is constrained by {@code stage}:
     * <ul>
     *   <li>PRE:  only PRE and NORMAL transitions (all if {@code item == NO_INPUT})</li>
     *   <li>POST: only POST transitions</li>
     * </ul>
     * <p>
     * Each time a transition is taken, a new {@link Checkpoint} is created and this function
     * is called recursively (potentially with a new {@code stage}).
     * <p>
     * The intended result of a non-recursive call of this function is to follow all valid chains
     * from the start state that do or do not consume an item of input (depending on whether
     * {@code item == NO_INPUT}).
     * <p>
     * A valid chain is either: PRE* NORMAL POST* (input-consuming chain) or PRE* POST+
     * (non-consuming chain).
     * <p>
     * Whenever such a chain reachs its end, the corresponding checkpoint is merged at input
     * position {@code pos+1}. Then a recursive call checks whether another non-consuming chain
     * can be formed after the previous chain (multiple such non-consuming chains can follow
     * each other).
     */
    private void advance (Checkpoint source, int stage, Object item)
    {
        State state = source.state;
        boolean continued = false; // was any transition taken from this state?

        for (Transition transition: state.transitions)
        {
            int stage1 = stage;

            switch (transition.type) {
                case PRE:
                    if (stage == POST) continue;
                    break;
                case NORMAL:
                    if (stage == POST || item == NO_INPUT || !transition.predicate.test(item))
                        continue;
                    stage1 = POST;
                    break;
                case POST:
                    // NO_INPUT is allowed to go from PRE to POST without consuming an item
                    if (stage == PRE && item != NO_INPUT) continue;
                    stage1 = POST;
                    break;
                default:
                    throw new Error();
            }

            Checkpoint next = new Checkpoint(transition.target, source.start, pos+1, source.regex);
            next.add_transition(source, transition);
            advance(next, stage1, item);
            continued = true;
        }

        if (!continued && stage == POST)
        {
            checkpoints.add(pos+1, source);
            // What if the input ended here?
            // Might create a new checkpoint at the same position (but in a different state).
            advance(source, PRE, NO_INPUT);
        }
    }

    // =============================================================================================

    /**
     * Generates a {@link MatchTree} object based for the match ending at the supplied checkpoint
     * and input position. The tree is select as specified in {@link MatchStream#trees()}.
     */
    MatchTree tree (Checkpoint checkpoint, int end)
    {
        if (checkpoint == null) return null;

        // 1. Extract the transition trace.

        ArrayList<Transition> trace = new ArrayList<>();

        while (checkpoint.transition_count() > 0)
        {
            trace.add(checkpoint.transition(0));
            checkpoint = checkpoint.transition_source(0);
        }

        Collections.reverse(trace);

        // 2. Replay the transition trace.

        MatchTree match = new MatchTree(checkpoint.regex, checkpoint.start, end);

        int trace_i = 0;
        int input_i = checkpoint.start;

        while (trace_i < trace.size())
        {
            // The next input item or NO_INPUT if we're over the input size.
            Object item = input_i < input.size() ? input.get(input_i) : NO_INPUT;

            // play PRE actions
            while (trace_i < trace.size())
            {
                Transition t = trace.get(trace_i);
                if (t.type != Transition.PRE) break;
                ++trace_i;
                t.action.accept(match, item);
            }

            // play NORMAL action
            if (trace_i < trace.size())
            {
                Transition t = trace.get(trace_i);

                if (t.type == Transition.NORMAL)
                {
                    if (item == NO_INPUT)
                        throw new IllegalStateException("The trace does not match with the input.");
                    ++input_i;
                    ++trace_i;
                    t.action.accept(match, item);
                }
            }

            // play POST actions
            while (trace_i < trace.size())
            {
                Transition t = trace.get(trace_i);
                if (t.type != Transition.POST) break;
                ++trace_i;
                t.action.accept(match, item);
            }
        }

        return match;
    }

    // ---------------------------------------------------------------------------------------------
}
