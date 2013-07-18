(defproject chlorine-repl "0.0.5"
  :description "Running Chlorine REPLs over nREPL."
  :url "http://github.com/chlorinejs/chlorine-repl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [chlorine "1.6.1"]
                 [core-cl2 "0.8.0"]
                 [myguidingstar/clansi "1.3.0"]]

  :injections [(require 'chlorine.repl)]
  :repl-options {:nrepl-middleware [chlorine.repl/wrap-chlorine-repl]}

  :scm {:url "git@github.com:chlorinejs/chlorine-repl.git"})
