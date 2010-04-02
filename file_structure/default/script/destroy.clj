(ns generate 
  (:require [destroyers.fixture-destroyer :as fixture-destroyer]
            [destroyers.migration-destroyer :as migration-destroyer]
            [destroyers.view-destroyer :as view-destroyer]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.controller-test-destroyer :as controller-test-destroyer]
            [destroyers.model-destroyer :as model-destroyer]
            [destroyers.model-test-destroyer :as model-test-destroyer]
            [destroyers.view-test-destroyer :as view-test-destroyer]
            [destroyers.xml-view-destroyer :as xml-view-destroyer]))

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
  (let [destroyer-namespace (find-ns (symbol (str "destroyers." command)))]
    (if destroyer-namespace)
      (let [destroyer-fn (ns-resolve destroyer-namespace 'destroy)]
        (if destroyer-fn
          (destroyer-fn params)
          (print-invalid-destroyer destroyer-namespace)))
      (print-unknown-command command)))

(let [destroy-command (first *command-line-args*)
      destroy-type-params (rest *command-line-args*)]
  (if destroy-command
    (destroy destroy-command destroy-type-params)
    (print-usage)))