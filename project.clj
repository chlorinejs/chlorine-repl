(defproject chlorine/repl "0.1.0-SNAPSHOT"
  :description "Running Chlorine REPLs over nREPL."
  :url "http://github.com/chlorinejs/chlorine-repl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [chlorine/js "1.6.4-SNAPSHOT"]
                 [chlorine/core-cl2 "0.9.0-SNAPSHOT"]
                 [myguidingstar/clansi "1.3.0"]]

  :injections [(require 'chlorine.repl)]
  :repl-options {:nrepl-middleware [chlorine.repl/wrap-chlorine-repl]}

  :scm {:url "git@github.com:chlorinejs/chlorine-repl.git"})
