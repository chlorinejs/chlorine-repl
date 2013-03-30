(ns chlorine.repl
  (:use [chlorine.js :only [js-emit
                            *temp-sym-count* *last-sexpr* *print-pretty*]]
        [evaljs.core]
        [evaljs.rhino]
        [chlorine.util :only [format-code]]
        [clansi.core :only [*use-ansi* style]])
  (:require [clojure.tools.nrepl :as nrepl]
            (clojure.tools.nrepl [transport :as transport]
                                 [server :as server]
                                 [misc :refer (returning)]
                                 [middleware :refer (set-descriptor!)]))
  (:import clojure.lang.LineNumberingPushbackReader
           java.io.StringReader))

(def ^:dynamic *cl2-repl-env* nil)
(def ^:dynamic *eval* nil)
(def ^:dynamic *cl2-ns* 'cl2)
(def ^:dynamic *cl2-repl-options* nil)

(def temp-sym-count (ref 999))
(def last-sexpr (ref nil))

(def rhino-session (rhino-context))

(defn launch-cl2-repl
  [repl-env eval]
  (set! *cl2-repl-env* repl-env)
  (set! *eval* eval)
  (set! *cl2-ns* 'cl2)
  (dosync (ref-set temp-sym-count 999)
          (ref-set last-sexpr nil))
  (print "Type `")
  (pr :cl2/quit)
  (println "` to stop the Chlorine REPL"))

(defn quit-cl2-repl
  []
  (set! *cl2-repl-env* nil)
  (set! *eval* nil)
  (set! *cl2-ns* 'cl2))

(defn browser-eval [expr]
  (binding [*temp-sym-count* temp-sym-count
            *last-sexpr*     last-sexpr
            *print-pretty*   true]
    (try (let [transcripted (with-out-str (js-emit expr))
               evaluated  (binding [*context* rhino-session]
                            (evaljs transcripted))]
           (println (style
                     (str "#<" transcripted ">")
                     :blue))
           (println (style (format-code evaluated) :green)))
         (catch Throwable e
           (println e)))))

(defn chlorine-eval
  [repl-env expr {:keys [verbose warn-on-undeclared special-fns]}]
  (let [is-special-fn? (set (keys special-fns))]
    (cond
     (= expr :cl2/quit) (do (quit-cl2-repl) "Goodbye!")

     (and (seq? expr) (is-special-fn? (first expr)))
     (apply (get special-fns (first expr)) repl-env (rest expr))

     :default
     (let [ret (browser-eval expr)]
       (try
         (read-string ret)
         (catch Exception _
           (when (string? ret)
             (println ret))))))))

(defn wrap-exprs
  [& exprs]
  (for [expr exprs]
    `(#'*eval* @#'*cl2-repl-env*
               '~expr
               @#'*cl2-repl-options*)))

;;(update-in {:a 1 :special-fns {'foo "foo"}} [:special-fns] merge {'bar "bar"})
(defn chlorine-repl
  [& {:keys [repl-env eval] :as options}]
  (let [repl-env (or repl-env (rhino-context))
        eval (or eval #(apply chlorine-eval %&))]
    (set! *cl2-repl-options* (-> (merge {:warn-on-undeclared true} options)
                                  ;; (update-in _)
                                  (dissoc :repl-env :eval)))
    (launch-cl2-repl repl-env eval)))

(defn prep-code
  [{:keys [code session] :as msg}]
  (let [code (if-not (string? code)
               code
               (let [reader (LineNumberingPushbackReader. (StringReader. code))
                     end (Object.)]
                 (->> #(binding [*ns* (create-ns *cl2-ns*)]
                         (try
                           (read reader false end)
                           (catch Exception e
                             (binding [*out* (@session #'*err*)]
                               (println (.getMessage e))
                               ::error))))
                   repeatedly
                   (take-while (complement #{end}))
                   (remove #{::error}))))]
    (assoc msg :code (apply wrap-exprs code))))

(defn cl2-ns-transport
  [transport]
  (reify clojure.tools.nrepl.transport.Transport
    (recv [this] (transport/recv transport))
    (recv [this timeout] (transport/recv transport timeout))
    (send [this resp]
      (let [resp (if (and *cl2-repl-env* (:ns resp))
                   (assoc resp :ns (str *cl2-ns*))
                   resp)]
        (transport/send transport resp)))))

(defn wrap-chlorine-repl
  [h]
  (fn [{:keys [op session transport] :as msg}]
    (let [cl2-active? (@session #'*cl2-repl-env*)
          msg (assoc msg :transport (cl2-ns-transport transport))
          msg (if (and cl2-active? (= op "eval")) (prep-code msg) msg)]
      ; ensure that bindings exist so cl2-repl can set! 'em
      (when-not (contains? @session #'*cl2-repl-env*)
        (swap! session (partial merge {#'*cl2-repl-env* *cl2-repl-env*
                                       #'*eval* *eval*
                                       #'*cl2-repl-options* *cl2-repl-options*
                                       #'*cl2-ns* *cl2-ns*})))

      (h msg))))

(def repl chlorine-repl)

(set-descriptor! #'wrap-chlorine-repl
  {:requires #{"clone"}
   :expects #{"eval"}
   :handles {}})
