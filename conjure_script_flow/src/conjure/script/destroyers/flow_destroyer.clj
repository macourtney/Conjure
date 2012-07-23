(ns conjure.script.destroyers.flow-destroyer
  (:require [clojure.tools.logging :as logging]
            [conjure.flow.util :as util]
            [conjure.script.destroyers.flow-test-destroyer :as flow-test-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy flow command."}
  usage []
  (println "You must supply a service (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj flow <service>"))

(defn
#^{:doc "Destroys the flow file from the given service."}
  destroy-flow-file [service silent]
  (if service
    (if-let [flows-directory (util/find-flows-directory)]
      (if-let [flow-file (util/find-flow-file flows-directory service)]
        (let [is-deleted (.delete flow-file)] 
          (logging/info (str "File " (.getName flow-file) (if is-deleted " destroyed." " not destroyed."))))
        (logging/info "Flow file not found. Doing nothing."))
      (do
        (logging/error (str "Could not find flows directory.: " flows-directory))
        (logging/error "Command ignored.")))
    (when (not silent) (usage))))

(defn
#^{:doc "Destroys a flow file for the service name given in params."}
  destroy [params]
  (destroy-flow-file (first params) false))

(defn
#^{:doc "Destroys all of the files created by the flow_generator."}
  destroy-all-dependencies
  [{ :keys [service actions silent] :or { actions (), silent false } }]
    (destroy-flow-file service silent)
    (flow-test-destroyer/destroy-all-dependencies service silent))