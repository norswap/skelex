# skelex

- [Maven Dependency][jitpack]
- [User Manual][manual]
- [Javadoc (1.0.0)][javadoc]
- [Javadoc (latest)][snapdoc]

[manual]: /doc/README.md
[jitpack]: https://jitpack.io/#norswap/skelex
[javadoc]: https://jitpack.io/com/github/norswap/skelex/-1.0.0-g8036bda-1/javadoc/index.html
[snapdoc]: https://jitpack.io/com/github/norswap/skelex/-SNAPSHOT/javadoc/

Skelex is a [regular expression] matching library with three specificities:

[regular expression]: https://en.wikipedia.org/wiki/Regular_expression

1. It works on stream of objects, instead of streams of characters as is usual.

2. It produces fully structure matching information (~ a parse tree). Usual regex matching
   engines can only match flat pieces of input. For instance, the regex `(a)*`, when applied
   on the input `aaa` will only capture the last repetition of `a`. skelex, on the other hand,
   would capture a list with three occurences of `a`.
   
3. Skelex is able to match many regexes at the same time on the same input.
   
## Installation 

If you are using Maven (or another popular JVM build tool), [see here][jitpack].

A self-contained JAR file is also available [here][jar] as part of [a release] that also
includes sources and javadoc.

[jar]: https://github.com/norswap/skelex/releases/download/1.0.0/skelex-1.0.0-fatjar.jar
[a release]: https://github.com/norswap/skelex/releases

## Usage

Here is a toy usage example:

```
import norswap.skelex.*;
import java.util.Arrays;
import java.util.List;

import static norswap.skelex.DSL.*;

public class Test
{
    public static void main (String[] args)
    {
        Runner runner = new Runner();
        
        Regex expr = seq(Integer.class, zeromore(choice("+", "-", "*", "/"), Integer.class));
        runner.add(expr);
        
        List<?> input = Arrays.asList(1, "+", 2, "-", 3);
        runner.advance(input);

        MatchTree match = runner.matches().longest_tree();
        System.out.println(match);
        // prints the match: [1, [[(0, +), 2], [(1, -), 3]]]
    }
}
```

For fairly simple operations, one-liner versions are available through the [Skelex] class.

[Skelex]: https://jitpack.io/com/github/norswap/skelex/-SNAPSHOT/javadoc/norswap/skelex/Skelex.html

## Roadmap

- Add lazy repetition operators.
- [Random generation testing](http://norswap.com/gen-testing/)
- Simplify / speed-up the transition graphs somewhat.