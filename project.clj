(defproject chlorine-repl "0.0.1"
  :description "Running Chlorine REPLs over nREPL."
  :url "http://github.com/myguidingstar/chlorine-repl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.nrepl "0.2.0-beta9"]
                 [chlorine "1.5.2"]
                 ]

  :injections [(require 'chlorine.repl)]
  :repl-options {:nrepl-middleware [chlorine.repl/wrap-chlorine-repl]}

  :scm {:url "git@github.com:myguidingstar/chlorine-repl.git"})
