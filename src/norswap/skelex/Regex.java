package norswap.skelex;

/**
 * A regular expression for use with the Skelex library.
 * <p>
 * While the constructor is public, users of the library should not subclass this class,
 * as the library only works with the subclasses defined within the {@link norswap.skelex.regex}
 * package.
 */
public abstract class Regex
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Cached automaton.
     */
    private Automaton automaton;

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the automaton corresponding to this regex, creating it if necessary.
     */
    Automaton automaton() {
        return automaton != null
            ? automaton
            : (automaton = AutomatonBuilder.build_automaton(this));
    }

    // ---------------------------------------------------------------------------------------------
}
