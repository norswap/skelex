# SKELEX USER MANUAL

For information about how Skelex is implemented, see the [Implementation
Guide](/doc/implementation.md). Read the user manual first!

For brevity's sake, I'm going to assume the reader is familiar with what [regular expressions]
are, at least in the context of matching strings. Before you go on, you should be able to understand
the meaning of the regex `a*(b|c)+d?`.

[regular expressions]: https://en.wikipedia.org/wiki/Regular_expression

This manual does not give full usage details, rather it is a complement to the [Javadoc], and
focuses on concepts.

[Javadoc]: https://jitpack.io/com/github/norswap/skelex/-SNAPSHOT/javadoc/

## Motivation

Skelex has three peculiarities when compared with other regex-matching libraries:

1. It works on streams of objects, instead of streams of characters as is usual.

2. It can produce full structure matching information (~ a parse tree). Usual regex matching
   engines can only match flat pieces of input. For instance, the regex `(a)*`, when applied
   on the input `aaa` will only capture the last repetition of `a`. skelex, on the other hand,
   would capture a list with three occurences of `a`.
   
3. Skelex is able to match many regexes at the same time on the same input.

Finally, we note that the computational complexity of matching a fixed number of regexes over
an input of size `n` is `O(n)`.

Not exactly what you need? Wondering about alternatives? Check the [Alternatives](#alternatives)
section at the bottom of the file.

## Creating Regexes

In Skelex, all regexes are modelled using subclasses of the abstract `Regex` class.
Suclasses are found in the `norswap.skelex.regex` package.

Direct instantiation is possible, although users are encouraged to use static methods
from the `DSL` class instead.

By themselves, these classes only model the structure of the regular expression.
They don't actually implement any functionality.

**Important:** regexes are compared based on *identity*. This means that using the same expression
twice will yield two distinct regexes.

## Basic Workflow, Registrations & Matches

In order to match a regex against some input, the user needs an instance of `Runner`. He then
supplies one or more regexes to this runner (`Runner#add`) and some input (`Runner#advance`).

The runner keeps track of the input it has seen. The *current input position* refers to the amount
of input seen so far.

Each regex added to the runner is added at an input position from where the runner will attempt to
match the regex. This position must be bigger or equal to the current position.

Together, the pair `(regex, start position)` is said to form a *regex registration*, or just
**registration** for short. Registrations are unique: adding the same regex at the same position does
nothing.

At any point, you can request match results *up to an input position* with the `matches` methods.
This will return a `MatchStream` object that you can use to extract the results you are interested
in. All the results in the stream will be match information about registrations who match a portion
of the input ending at the requested position. (Given how regular expressions work, note the same
registration can have multiple matches ending at different positions.)

In particular, `MatchStream` lets you filter the results by regex, registration and end position
(not useful when getting `MatchStream` from a `Runner`).

A `MatchStream` lets you get two kind of matches:

- `Match` objects describe the extent of a match: that is, a
  `(regex, start position, end position)` triplet.
  
- `MatchTree` objects extend `Match` with a description of how the input structurally matches the
   regex (a kind of parse tree). So for instance the regex `(ab)*(c|d)` applied on the input `ababc`
   would yield the tree `((ab, ab), (0, c))`.

The trees within a `MatchTree` are made of three components:
 
- lists: matching the sub-matches in sequences and repetitions
- `Branch` object: matching a particular choice alternative
- `null` values: matching an empty optional regex

Refer to the documentation of `MatchTree` and `Branch` for more usage details.

### Tree Ambiguity

Some regexes are ambiguous and have a potentially exponential number of match trees for the same
input span. Examining all these trees is not practical, so instead we return at most one tree per
registration.

This tree is selected as follow:

- We prioritize the presence of an input item over its absence (so repetitions prefer matching more,
  choices prefer matching rather than skipping).
- We prioritize choice alternatives in the order they appear in the regex.
- Earlier sub-regexes have precedence over the later.

So for instance, the regex `a+a+(b|b)` on input `aaab` will always match as `(aa, a, (0, b))`.

If multiple trees are feasible, one can get the desired tree by reworking the regex, exploiting
in particular the order of alternatives within choices.

Later down the line, I plan on adding lazy repetition to Skelex: repetitions who prefer to match
less input. This will give a bit more flexibility in selecting the desired trees.

### Registration Ambiguity

Some of the `MatchStream` methods return a single match (or a single match per regex, etc), but the
registration they target is actually ambiguous. To resolve it, we use the *longest criterion*: the
registration made earliest in the input wins. If ambiguity still persists, the chronologically
earliest registration wins.

## Alternatives

If you do not care about any of Skelex' specificities, other libraries will probably yield much
better performances ([dk.brics.automaton] is  highly regarded). It is probably possible to get
Skelex to go much faster through internal optimization and/or slight interface changes, but that is
not a priority right now.

I do not know of any other libraries combining Skelex' three specificities. Finding libraries that
perform regex matching on objects is hard enough, but there is at least one called [openregex].

There might be full-blown [CFG] or [PEG] parsers that can offer (some of) the same capabilities, but
I haven't really investigated the issue. They can't be found via an obvious google search; I don't
know any of them, [and I know quite a few][parsers].

If you know of anything similar, please let me know!

[dk.brics.automaton]: http://www.brics.dk/automaton/
[openregex]: https://github.com/knowitall/openregex
[CFG]: https://en.wikipedia.org/wiki/Context-free_grammar
[PEG]: https://en.wikipedia.org/wiki/Parsing_expression_grammar
[parsers]: https://github.com/norswap/whimsy/blob/master/doc/autumn/notes/parsing-tools.md