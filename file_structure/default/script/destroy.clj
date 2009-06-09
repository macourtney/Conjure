(ns generate 
  (:require [destroyers.migration-destroyer :as migration-destroyer]
            [destroyers.view-destroyer :as view-destroyer]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.model-destroyer :as model-destroyer]))

(defn
#^{:doc "Prints the usage information to standard out."}
  print-usage []
  (println "Usage: ./run.sh script/destroy.clj <destroy type> <destroy params>*"))

(defn
#^{:doc "Warns the user of an unknown command and prints the usage information to standard out."}
  print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn
#^{:doc "Determines which command was passed and calls the appropriate destroy method."}
  destroy [command params]
  (cond 
    (. command equals "migration")
      (migration-destroyer/destroy-migration params)
      
    (. command equals "view")
      (view-destroyer/destroy-view params)
      
    (. command equals "controller")
      (controller-destroyer/destroy-controller params)
      
    (. command equals "model")
      (model-destroyer/destroy-model params)
      
    true ; Default condition.
      (print-unknown-command command)))


(let [command (first *command-line-args*)
      destroy-args (rest *command-line-args*)]
  (if command
    (let [destroy-command (first destroy-args)
          destroy-type-params (rest destroy-args)]
      (if destroy-command
        (destroy destroy-command destroy-type-params)
        (print-usage)))

    (do
      (println "Nil command, cannot continue.")
      (print-usage))))