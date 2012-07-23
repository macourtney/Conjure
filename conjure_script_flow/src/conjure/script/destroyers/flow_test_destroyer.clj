(ns conjure.script.destroyers.flow-test-destroyer
  (:require [clojure.tools.logging :as logging]
            [conjure.core.test.util :as util]
            [clojure.tools.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the destroy flow test command."}
  usage []
  (println "You must supply a service (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj flow-test <service>"))

(defn
#^{:doc "Destroys the flow file from the given service."}
  destroy-flow-test-file [service silent]
  (if service
    (if-let [functional-test-directory (util/find-functional-test-directory)]
      (if-let [functional-test-file (util/functional-test-file service functional-test-directory)]
        (let [is-deleted (.delete functional-test-file)] 
          (logging/info (str "File " (.getName functional-test-file) (if is-deleted " destroyed." " not destroyed.")))
          (when is-deleted (file-utils/delete-if-empty functional-test-directory)))
        (logging/info "Flow test file not found. Doing nothing."))
      (do
        (logging/info "Could not find the functional test directory.")
        (logging/info "Command ignored.")))
    (usage)))

(defn
#^{:doc "Destroys a flow test file for the service name given in params."}
  destroy [params]
  (destroy-flow-test-file (first params) false))

(defn
#^{:doc "Destroys all of the files created by the flow_test_generator."}
  destroy-all-dependencies 
  ([service silent]
    (destroy-flow-test-file service silent)))