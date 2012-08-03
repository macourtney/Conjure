(ns conjure.util.execute-utils
  (:import [java.io FileNotFoundException]))

(defn print-usage []
  (println "Usage: lein conjure <command> <command parameters>*"))

(defn print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn print-invalid-script [script-namspace]
  (println (str "Invalid script: " script-namspace ". The script must implement a run function.")))

(defn run-script [command params]
  (let [script-namspace-symbol (symbol (str "conjure.script." command))]
    (try
      (require script-namspace-symbol)
      (catch FileNotFoundException e))
    (if-let [script-namespace (find-ns script-namspace-symbol)]
      (if-let [script-fn (ns-resolve script-namespace 'run)]
        (script-fn params)
        (print-invalid-script script-namespace))
      (print-unknown-command command))))

(defn run-args [args]
  (if (empty? args)
    (run-script "server" [])
    (run-script (first args) (rest args))))