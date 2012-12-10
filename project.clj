(defproject chlorine-repl "0.0.1"
  :description "Running Chlorine REPLs over nREPL."
  :url "http://github.com/myguidingstar/chlorine-repl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.nrepl "0.2.0-RC1"]
                 [chlorine "1.6.0"]
                 [boot-cl2 "1.0.0"]
                 ]

  :injections [(require 'chlorine.repl)]
  :repl-options {:nrepl-middleware [chlorine.repl/wrap-chlorine-repl]}

  :scm {:url "git@github.com:myguidingstar/chlorine-repl.git"})
