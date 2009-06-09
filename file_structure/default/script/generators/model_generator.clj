(ns generators.model-generator
  (:require [generators.migration-generator :as migration-generator]
            [conjure.model.model :as model]
            [conjure.util.string-utils :as string-utils]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate model command."}
  model-usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj model <model>"))
  
(defn
#^{:doc "Generates the migration file for the model."}
  generate-migration-file [model]
  (migration-generator/generate-migration-file (model/migration-for-model model)))
  
(defn
#^{:doc "Generates the model content and saves it into the given model file."}
  generate-file-content [model-file]
      (let [model (model/model-from-file model-file)
            model-namespace (model/model-namespace model)
            content (str "(ns " model-namespace ")")]
        (file-utils/write-file-content model-file content)
        (generate-migration-file model)))

(defn
#^{:doc "Creates the model file associated with the given model."}
  generate-model-file
    ([model]
      (if model
        (let [models-directory (model/find-models-directory)]
          (if models-directory
            (let [model-file (model/create-model-file models-directory model)]
                (if model-file
                  (generate-file-content model-file)))
            (do
              (println "Could not find models directory.")
              (println models-directory))))
        (model-usage))))
        
(defn 
#^{:doc "Generates a model file for the model name in params."}
  generate-model [params]
  (generate-model-file (first params)))