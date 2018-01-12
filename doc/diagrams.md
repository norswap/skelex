# Automaton Diagrams

This file contains graphical ASCII-art illustration of the automatons
created when compiling different types of regular expressions.

In these diagrams:

- Square boxes represent states.
- `s` is the start state of the automaton
- `e` is the end state of the automaton
- `-->--` represents a transition
- `==>==` represent potentially multiple transitions that are part of a sub-automaton
- Sub-automatons are designed by `A`, `B`.
- The start/end states of a sub-automaton by `As`, `Bs` / `Ae`, `Be`.

These diagrams were produced using a combination of http://asciiflow.com and GNU Emacs.

## Sequence

We assume there are two items in the sequence: `A` and `B`.

Notice the start state of `B` disappears, as it is merged with the end state of `A`.

```
+---+  1  +----+     +----+     +----+  2  +---+
| s |-->--| As |==>==| Ae |==>==| Be |-->--| e |
+---+     +----+     +----+     +----+     +---+

1: PRE  - mark
2: POST - collect 
```

## Choice

We assume there are two alternatives: `A` and `B`.

```
             1  +----+     +----+  3
          +-->--| As |==>==| Ae |-->--+
+---+     |     +----+     +----+     |     +---+
| s |-->--+                           +-->--| e |
+---+     |  2  +----+     +----+  4  |     +---+
          +-->--| Bs |==>==| Be |-->--+
                +----+     +----+
                
1, 2: PRE  - noop
3, 4: POST - push_branch(0/1)
```

## Maybe

```
+---+    1    +---+    2    +---+
| s |---->----| m |---->----| e |
+---+         +---+         +---+
  |                           |
  |  3  +-+--+     +----+  4  |
  +-->--| As |==>==| Ae |-->--+
        +-+--+     +----+
        
1: PRE  - push null
2: POST - noop
3: PRE  - noop
4: POST - noop
```

## ZeroMore

```
 +---+  1  +-----+  2  +----+  3  +----+
 | s +-->--|  m  |-->--| e1 |-->--| e2 |
 +---+     +-+-+-+     +----+     +----+
        4    | |    5
+-------<----+ +----<-------+
|                           |
|     +-+--+     +----+     |
+-->--| As |==>==| Ae |-->--+
      +-+--+     +----+
 
1: PRE  - push list
2: PRE  - noop
3: POST - noop
4: PRE  - noop
5: POST - accrete
```

## OneMore

```
+---+  1  +-+--+     +----+  2  +---+
| s |-->--| As |==>==| Ae |-->--| e |
+---+     +-+--+     +--+-+     +---+
            |     3     |
            +-----<-----+

1: PRE  - push list
2: POST - accrete
3: POST - accrete
```