(ns chlorine.node
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [cheshire.core :refer [parse-string generate-string]]
            [chlorine.repl :as cl2-repl])
  (:import java.io.PipedReader
           java.io.PipedWriter))

(defn- load-as-tempfile
  "Copy a file from the classpath into a temporary file.
  Return the path to the temporary file."
  [filename]
  (let [tempfile (java.io.File/createTempFile "cl2repl" ".js")
        resource (io/resource filename)]
    (.deleteOnExit tempfile)
    (assert resource (str "Can't find " filename " in classpath"))
    (with-open [in (io/input-stream resource)
                out (io/output-stream tempfile)]
      (io/copy in out))
    (.getAbsolutePath tempfile)))

(defn- output-filter
  "Take a reader and wrap a filter around it which swallows and
  acts on output events from the subprocess. Keep the filter
  thread running until alive-func returns false."
  [reader alive-func & [out-func]]
  (let [pipe (PipedWriter.)]
    (future
      (while (alive-func)
        (let [line (.readLine reader)
              data (parse-string line)]
          (if-let [output (get data "output")]
            (if out-func (out-func output)
                (do (print output) (flush)))
            (doto pipe
              (.write (str line "\n"))
              (.flush))))))
    (io/reader (PipedReader. pipe))))

(defn- process-alive?
  "Test if a process is still running."
  [^Process process]
  (try (.exitValue process) false
       (catch IllegalThreadStateException e true)))

(defn launch-node-process
  "Launch the Node subprocess."
  [& [output-func]]
  ;; Launch repl.js through an eval to trick Node into thinking it was
  ;; started from the current directory, allowing require() to work as
  ;; expected.
  (let [launch-script
        (str "eval(require('fs').readFileSync('"
             (string/replace (load-as-tempfile "public/node_repl.js") "\\" "/")
             "','utf8'))")
        process (let [pb (ProcessBuilder. ["node" "-e" launch-script])]
                  (.start pb))]
    {:process process
     :input (output-filter (io/reader (.getInputStream process))
                           #(process-alive? process)
                           output-func)
     :output (io/writer (.getOutputStream process))
     :loaded-libs (atom #{})}))

(defn node-eval [repl-env expr]
  (let [result
        (let [{:keys [input output]} repl-env]
          (.write output (str (generate-string {:file "nofile.js"
                                                :line 0
                                                :code expr})
                              "\n"))
          (.flush output)
          (let [result-string (.readLine input)]
            (parse-string result-string true)))]
    (if-let [error (:error result)]
      {:status :exception :value (:stack error)}
      {:status :success :value (:result result)})))

(defn node-tear-down [repl-env]
   (let [process (:process repl-env)]
     (doto process
       (.destroy)
       (.waitFor))))