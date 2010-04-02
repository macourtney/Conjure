(ns destroyers.model-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.model.util :as util]
            [destroyers.migration-destroyer :as migration-destroyer]
            [destroyers.model-test-destroyer :as model-test-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy model command."}
  model-usage []
  (println "You must supply a model (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj model <model>"))
  
(defn
#^{:doc "Destroys the create migration file associated with the given model."}
  destroy-migration-for-model [model]
  (migration-destroyer/destroy-all-dependencies (util/migration-for-model model)))

(defn
#^{:doc "Destroys the model file from the given model."}
  destroy-model-file [model]
  (if model
    (let [models-directory (util/find-models-directory)]
      (if models-directory
        (let [model-file (util/find-model-file models-directory model)]
          (if model-file
            (let [is-deleted (. model-file delete)] 
              (logging/info (str "File " (. model-file getPath) (if is-deleted " deleted." " not deleted."))))
            (logging/info "Model file not found. Doing nothing.")))
        (do
          (logging/error (str "Could not find models directory: " models-directory))
          (logging/error "Command ignored."))))
    (model-usage)))

(defn
#^{:doc "Destroys a model file for the model name given in params."}
  destroy [params]
  (destroy-model-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the model_generator."}
  destroy-all-dependencies
  ([model]
    (destroy-model-file model)
    (destroy-migration-for-model model)
    (model-test-destroyer/destroy-all-dependencies model)))