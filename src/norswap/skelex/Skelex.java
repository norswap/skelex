package norswap.skelex;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Static methods that enable one-liner formulation of common operations.
 */
public final class Skelex
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a match stream for all matches of {@code regex} covering the whole input.
     */
    public static MatchStream match_exactly (Regex regex, List<?> input)
    {
        Runner runner = new Runner();
        runner.add(regex);

        for (Object it: input) {
            runner.advance(it);
            if (runner.dead())
                return new MatchStream(Stream.empty(), runner);
        }

        return runner.matches();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a match stream for all matches of {@code regex} within the input.
     */
    public static MatchStream matches_anywhere (Regex regex, List<?> input)
    {
        Runner runner = new Runner();

        Stream<Checkpoint> stream = input.stream()
            .map(it -> {
                runner.add(regex);
                runner.advance(it);
                return runner.stream();
            })
            .flatMap(Function.identity());

        return new MatchStream(stream, runner);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a match stream for all matches of {@code regex} starting at the beginning
     * of the input.
     */
    public static MatchStream matches_from_start (Regex regex, List<?> input)
    {
        Runner runner = new Runner();
        runner.add(regex);

        ArrayList<Stream<Checkpoint>> streams = new ArrayList<>(input.size());

        for (Object it: input) {
            runner.advance(it);
            if (runner.dead()) break;
            streams.add(runner.stream());
        }

        Stream<Checkpoint> stream = streams.stream().flatMap(Function.identity());
        return new MatchStream(stream, runner);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a match stream for all matches of {@code regex} ending at the end of the input.
     */
    public static MatchStream matches_at_end (Regex regex, List<?> input)
    {
        Runner runner = new Runner();
        runner.add(regex);

        for (Object item: input) {
            runner.advance(item);
            runner.add(regex);
        }

        return runner.matches();
    }

    // ---------------------------------------------------------------------------------------------
}
