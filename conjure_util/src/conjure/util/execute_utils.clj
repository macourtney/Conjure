(ns conjure.util.execute-utils
  (:import [java.io FileNotFoundException]))

(defn print-usage []
  (println "Usage: lein conjure <command> <command parameters>*"))

(defn print-unknown-command [command]
  (println "Unknown command: " command)
  (print-usage))

(defn print-invalid-script [script-namespace]
  (println "Invalid script:" script-namespace " The script must implement a run function."))

(defn run-script [command params]
  (let [script-namspace-symbol (symbol (str "conjure.script." command))]
    (try
      (require script-namspace-symbol)
      (catch FileNotFoundException e
        (println "Could not find the namespace for command:" command)))
    (if-let [script-namespace (find-ns script-namspace-symbol)]
      (if-let [script-fn (ns-resolve script-namespace 'run)]
        (script-fn params)
        (print-invalid-script script-namspace-symbol))
      (print-unknown-command command))))

(defn run-args [args]
  (if (empty? args)
    (run-script "server" [])
    (run-script (first args) (rest args))))