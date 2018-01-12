# Implementation Notes

This file gives a tour of the Skelex implementation.

This assumes you have read the [user manual], which explains the key concepts in Skelex.

[user manual]: /doc/README.md

## Automatons

Internally, a regex is compiled into an `Automaton` before being matched. The process is transparent
to the user: when the regex is first added to a `Runner`, it is compiled and the
automaton is cached inside the regex. Only the automatons of regexes added to a runner get cached:
their sub-regexes do not get the same treatment.

- Automatons are made of states and transitions.
- Each automaton has a start and an end state.
- A state may have any number of incoming or outgoing transitions.
- Only the outgoing transitions are stored in a state.
- Transitions records their target state. 
- The end state of an automaton may not have any outgoing transitions.

The end state of the automaton is the one that will be connected to subsequent or surrounding
automatons when its regex is used as a sub-regex.

A state is said to be *accepting* when it has no outgoing transition and is checkpointable (see
later). Reaching an accepting state indicates that the regex corresponding to the automaton was
matched.

In our implementation, all end states are also accepting states, although they are not required
to be (they might not be checkpointable).

Like regexes, automatons do not have any functionality: they simply define a graph of state
and transitions that is used by a `Runner` to perform the actual matches.

Note that these automatons have very little to do with the Non-deterministic Finite Automaton (NFA)
and Deterministic Finite Automaton (DFA) from the classical litterature.

The conversion between regex and automaton is performed by the class `AutomatonBuilder`.

We will now go into more detail into the composition of an automaton and its meaning.

## States and Transitions

A state is simply an identity associated with a set of outgoing transitions.

States can be mutated by adding new transitions, but it is guaranteed that we will never
mutate a state that is part of an automaton returned to the user.

In addition to its target state, a transition possesses a predicate that constraints whether the
transition can be taken, and an action that helps build `MatchTree` objects.

There are three kinds of transitions: PRE, NORMAL and POST.

Intuitively, NORMAL transitions consume an item of input. PRE and POST do not consume input but
can only be taken if their associated predicate is satisfied.

If an item is to be consumed, PRE transitions are logically related to the NORMAL transition(s) they
make reachable, while POST transitions are logically related to the NORMAL transition(s) through
which they are reached. PRE and POST transition may also occur without intervening NORMAL
transitions, more on this later.

## Anchor States & Transition Chains

To further understand the three kind of transitions, it is necessary to define the notion of *anchor
state*.

A state is an anchor state if it is the start state of an automaton, or if it is reached by one of
these two chains of transitions:

1. PRE* NORMAL POST*
2. PRE* POST+

The repetition operators in those chains are greedy: you can't stop after a PRE or POST transition
if another transition of the same kind can be taken (the predicate must match).

Anchor states are the states we should stop on after consuming an item of input, or before consuming
any input. Intuitively, the two chains above correspond to matching an item of input (1), or
considering the absence of an item of input / verifying that some condition holds without looking at
some input (2).

It wouldn't be correct to stop after a PRE transition, because it means we have started down a
chain, but haven't performed all the checks pertaining to the input item (or absence of input item)
being considered.

Note the PRE and POST transitions in both chains may belong to multiple levels of nested regexes.
For instance, the regex `ab`, is made of two sub-regexes `a` and `b` combined into a sequence. When
matching 'b' in that regex, the chain taken ends with a POST transition that logically belongs to
the sequence.

If all this is unclear, the best thing to do is to peruse the actual automatons being built for
different types of regexes (see `AutomatonBuilder` and `doc/diagrams.md`).

## Runner & Checkpoints

A runner keeps a current input position (initially 0) and a set of checkpoints (`Checkpoint`)
anchored to various input positions (initially there are no checkpoints). The mapping between
input positions and checkpoint is kept in an object of type `CheckpointMap`.

Checkpoints are linked to form reverse linked lists of traversed states. A checkpoint records a
state, the transitions and previous checkpoints that were used to reach that it, and the registration
that started these chains of checkpoints.

For each input position, the runner keeps track of a set of checkpoints: there can only be a single
checkpoint for a given state and registration pair. Those checkpoints always hold an anchor state.
There are also checkpoints in the linked lists that are not directly held by the runner. These
correspond to the intermediary non-anchor states.

Regexes can be added to the runner, but only at or beyond the current input position. Such an
addition causes the runner to add a new checkpoint at the specified input position, containing the
initial state of the regex's automaton. It also checks if the automaton can reach any other anchor
state without consuming any input, and adds those (if any) as checkpoints at the same position.

A runner can be fed input, either one item or many items at at time. Supplying many items simply
runs the logic for a single item repeatedly.

When an item of input is fed to the runner, the runner tries to advance the state in each checkpoint
at the current position. Only chains with NORMAL transitions are considered, since we want to
consume the input item. This potentially yields new checkpoints at the next input position. For each
such new checkpoints, the runner also checks if the automaton can reach any other anchor state
without consuming any inputs, and adds those (if any) as checkpoints at the same position. Finally,
the current input position is incremented.

## Automaton Highlights

I now make a few comments about some non-obvious properties of automatons.

- Beware of POST transitions mixed with other types of transitions.

   The issue is that if a state has, for instance, both a POST and PRE transition, then
   the state won't be treated as an anchor state if the POST transition can be taken.
   
   In theory, mixing POST and PRE/NORMAL transitions can be exploited to create automaton that
   descriminate based on a POST predicate, but this is not currently necessary.

- Anchor states must be reached by a NORMAL or POST transition (not a PRE).

   This is simply a consequence of the two chains defined in the "Anchor States" section.
   In the second chain, we require at least one POST transition in order to distinguish between
   chains that can succeed without consuming any inputs, and chain that will eventually reach a
   NORMAL transition.
   
   A state that cannot be reached by a NORMAL or POST transition is not checkpointable.

## Predicates & Runner Internals

Transition predicates have the type `Predicate<Object>`. When run, they are supplied with the input
item being considered, or with the special `NO_INPUT` value.

When the runner is fed a new input item, it looks at all the checkpointed states at the current
input position. For each of them, it tries to follow chains of transitions until an anchor state
is reached. The runner is able to determine that an anchor is reached based on two conditions:

1. No further transition can be taken (based on their predicates and the chain patterns explained
   earlier).
2. The last transition was a NORMAL or POST transition.

Crucially, the runner ensures that the input item was consumed by forbidding the second transition
chain when the input item is not `NO_INPUT`.

The reached anchor states are checkpointed, then the runner tries to reach other anchor states
without consuming any input (using the special `NO_INPUT` value). This process is iterated until no
more anchor states can be reached (and checkpointed).

For instance consider the regex `ab?c?`: when feeding the input item `a` at position 0, three new
anchor states can be reached, corresponding respectively to having matched `a`, `ab?` and `ab?c?`.

## Matching Traces

Both `Match` and `MatchTree` objects are not constructed eagerly when input items are supplied, but
rather on demand, when requested from a `MatchStream`. In both cases, the objects are generated
from a corresponding `Checkpoint`.

In particular, for `MatchTree` there are potentially an exponential number of trees for a single
registration and input span, and it is not possible to know how to construct the preferred
`MatchTree` object before the match completes. 

Even ignoring this issue, we assume that we might not need a `Match` or `MatchTree` object for every
registration in the runner, and so constructing them on demand is preferable.

Constructing `Match` objects from checkpoints is very easy, since each checkpoint records the
registration that gave rise to it. 

Constructing `MatchTree` objects is not so easy. This work is performed by the `Runner#tree` method.
This method starts by building a matching trace: a list of `Transition` taken to reach the
checkpoint's state. Then, that list is usually to "play" the trace: instantiating a new `MatchTree`
object, then running the action for each transition over the `MatchTree` object and the appropriate
input item. The role of these actions is to build up the tree into the structured representation
(or parse tree) corresponding to the match.

The input item passed to the action of each transition does not strictly match the input items
passed to the predicate of each transition: PRE and POST transitions that are related to a NORMAL
transition will see the input item consumed by that transition. Other PRE/POST transitions will
still see `NO_INPUT`.

Note that in some case, it is not obvious which checkpoint to build a `Match` or `MatchTree` from,
the disambiguation procedure is covered in the [user manual].