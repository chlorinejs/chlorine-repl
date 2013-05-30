# Chlorine-REPL

[nREPL](http://github.com/clojure/tools.nrepl) middleware that enables the
bootstrap of a [Chlorine](http://github.com/chlorinejs/chlorine) REPL on top of an nREPL
session.

## Requirements
You need NodeJs installed.

## Usage
Start nREPL from Leiningen:
```
lein repl
```

in the REPL, type
```
(chlorine.repl/repl)
```
There will be a welcome message like this:
```
Welcome to Chlorine REPL.
Type `(include! "r:/strategies/dev.cl2")` to load core library.
Type `:cl2/quit` to stop the Chlorine REPL
```
Enjoy!
