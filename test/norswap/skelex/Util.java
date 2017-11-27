package norswap.skelex;

import norswap.utils.Strings;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class Util
{
    /**
     * Returns a string representation of the automaton, which numbers its states and
     * indicates the PRE, NORMAL and POST transitions between these states.
     */
    public static String automaton_string (Automaton a)
    {
        int[] i = {0};
        HashMap<State, Integer> ids     = new HashMap<>();
        ArrayList<State>        states  = new ArrayList<>();
        ArrayDeque<State>       queue   = new ArrayDeque<>();
        StringBuilder           b       = new StringBuilder();

        queue.add(a.start);
        ids.put(a.start, i[0]++);
        states.add(a.start);

        while (!queue.isEmpty())
        {
            State state = queue.pop();
            int id = ids.get(state);

            b.append(id).append(state.transitions.isEmpty() ? ".\n" : ":\n");

            for (Transition t: state.transitions)
            {
                int target_id = ids.computeIfAbsent(t.target, k -> i[0]++);
                if (target_id == states.size())
                {
                    states.add(t.target);
                    queue.add(t.target);
                }

                switch (t.type) {
                    case Transition.PRE:
                        b.append("  -PRE-> "); break;
                    case Transition.NORMAL:
                        b.append("  -NOR-> "); break;
                    case Transition.POST:
                        b.append("  -POS-> "); break;
                }

                b.append(target_id).append("\n");
            }
        }

        Strings.pop(b, 1); // final newline
        return b.toString();
    }
}
