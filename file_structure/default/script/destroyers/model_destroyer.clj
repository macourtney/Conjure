(ns destroyers.model-destroyer
  (:require [conjure.model.model :as model]
            [destroyers.migration-destroyer :as migration-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy model command."}
  model-usage []
  (println "You must supply a model (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj model <model>"))
  
(defn
#^{:doc "Destroys the create migration file associated with the given model."}
  destroy-migration-for-model [model]
  (migration-destroyer/destroy-migration-file (model/migration-for-model model)))

(defn
#^{:doc "Destroys the model file from the given model."}
  destroy-model-file [model]
  (if model
    (let [models-directory (model/find-models-directory)]
      (if models-directory
        (let [model-file (model/find-model-file models-directory model)]
          (if model-file
            (do 
              (. model-file delete)
              (println "File" (. model-file getPath) "deleted.")
              (destroy-migration-for-model model))
            (println "Model file not found. Doing nothing.")))
        (do
          (println "Could not find models directory.")
          (println models-directory)
          (println "Command ignored."))))
    (model-usage)))

(defn
#^{:doc "Destroys a model file for the model name given in params."}
  destroy-model [params]
  (destroy-model-file (first params)))