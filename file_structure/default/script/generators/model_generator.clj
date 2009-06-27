(ns generators.model-generator
  (:require [generators.migration-generator :as migration-generator]
            [conjure.model.builder :as builder]
            [conjure.model.util :as util]
            [conjure.util.string-utils :as string-utils]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate model command."}
  model-usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj model <model>"))

(defn
#^{:doc "Returns the content for the up function of the create migration for the given model."}
  create-migration-up-content [model]
  (str "(database/create-table \"" (util/model-to-table-name model) "\" 
    (database/id))"))
    
(defn
#^{:doc "Returns the content for the down function of the create migration for the given model."}
  create-migration-down-content [model]
  (str "(database/drop-table \"" (util/model-to-table-name model) "\")"))
  
(defn
#^{:doc "Generates the migration file for the model."}
  generate-migration-file [model]
  (migration-generator/generate-migration-file 
    (util/migration-for-model model) 
    (create-migration-up-content model) 
    (create-migration-down-content model)))

(defn
#^{:doc "Generates the model content and saves it into the given model file."}
  generate-file-content [model-file]
      (let [model (util/model-from-file model-file)
            model-namespace (util/model-namespace model)
            content (str "(ns " model-namespace "
  (:use conjure.model.base
        clj-record.boot))

(init-model)")]
        (file-utils/write-file-content model-file content)
        (generate-migration-file model)))

(defn
#^{:doc "Creates the model file associated with the given model."}
  generate-model-file
    ([model]
      (if model
        (let [models-directory (util/find-models-directory)]
          (if models-directory
            (let [model-file (builder/create-model-file models-directory model)]
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