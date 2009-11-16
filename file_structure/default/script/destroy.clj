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

(defn
#^{:doc "Determines which command was passed and calls the appropriate destroy method."}
  destroy [command params]
  (cond 
    (. command equals "migration")
      (migration-destroyer/destroy-migration params)
      
    (. command equals "view")
      (view-destroyer/destroy-view params)

    (. command equals "view-test")
      (view-test-destroyer/destroy-view-test params)
      
    (. command equals "controller")
      (controller-destroyer/destroy-controller params)
      
    (. command equals "controller-test")
      (controller-test-destroyer/destroy-controller-test params)
      
    (. command equals "model")
      (model-destroyer/destroy-model params)

    (. command equals "model-test")
      (model-test-destroyer/destroy-model-test params)

    (. command equals "fixture")
      (fixture-destroyer/destroy-fixture params)

    (. command equals "xml-view")
      (xml-view-destroyer/destroy-xml-view params)
      
    true ; Default condition.
      (print-unknown-command command)))

(let [destroy-command (first *command-line-args*)
      destroy-type-params (rest *command-line-args*)]
  (if destroy-command
    (destroy destroy-command destroy-type-params)
    (print-usage)))