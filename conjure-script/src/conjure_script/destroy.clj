(ns conjure-script.generate 
  (:require [conjure.server.server :as server]))

(defn
#^{:doc "Prints the usage information to standard out."}
  print-usage []
  (println "Usage: ./run.sh script/destroy.clj <destroy type> <destroy params>*"))

(defn
#^{:doc "Warns the user of an unknown command and prints the usage information to standard out."}
  print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn print-invalid-destroyer [destroyer-namspace]
  (println (str "Invalid destroyer: " destroyer-namspace ". The destroyer must implement a destroy function.")))

(defn destroy [command params]
  (let [destroyer-namespace-symbol (symbol (str "destroyers." command "-destroyer"))]
    (require destroyer-namespace-symbol)
    (let [destroyer-namespace (find-ns destroyer-namespace-symbol)]
      (if destroyer-namespace
        (let [destroyer-fn (ns-resolve destroyer-namespace 'destroy)]
          (if destroyer-fn
            (destroyer-fn params)
            (print-invalid-destroyer destroyer-namespace)))
        (print-unknown-command command)))))

(server/init)

(let [destroy-command (first *command-line-args*)
      destroy-type-params (rest *command-line-args*)]
  (if destroy-command
    (destroy destroy-command destroy-type-params)
    (print-usage)))