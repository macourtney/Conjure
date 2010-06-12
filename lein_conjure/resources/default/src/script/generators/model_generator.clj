(ns generators.model-generator
  (:require [clojure.contrib.logging :as logging]
            [conjure.model.builder :as builder]
            [conjure.model.util :as util]
            [conjure.util.string-utils :as string-utils]
            [conjure.util.file-utils :as file-utils]
            [generators.migration-generator :as migration-generator]
            [generators.model-test-generator :as model-test-generator]))

(defn
#^{:doc "Prints out how to use the generate model command."}
  model-usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj model <model>"))

(defn
#^{:doc "Returns the content for the up function of the create migration for the given model."}
  create-migration-up-content [model]
  (str "(create-table \"" (util/model-to-table-name model) "\" 
    (id))"))
    
(defn
#^{:doc "Returns the content for the down function of the create migration for the given model."}
  create-migration-down-content [model]
  (str "(drop-table \"" (util/model-to-table-name model) "\")"))
  
(defn
#^{ :doc "Generates the migration file for the model." }
  generate-migration-file 
  ([model] (generate-migration-file model (create-migration-up-content model) (create-migration-down-content model)))
  ([model up-content down-content]
    (migration-generator/generate-migration-file 
      (util/migration-for-model model) 
      up-content 
      down-content)))

(defn
#^{ :doc "Creates the model file on disk" }
  model-file-content 
  ([model] (model-file-content model ""))
  ([model extra-content]
    (let [model-namespace (util/model-namespace model)]
      (str "(ns " model-namespace "
  (:use conjure.model.base
        clj-record.boot))

(clj-record.core/init-model)

" extra-content))))

(defn
#^{ :doc "Creates the model file on disk" }
  create-model-file 
  ([model] (create-model-file model (builder/create-model-file (util/find-models-directory) model)))
  ([model model-file] (create-model-file model model-file (model-file-content model)))
  ([model model-file model-content]
    (file-utils/write-file-content model-file model-content)))

(defn
#^{:doc "Generates the model content and saves it into the given model file."}
  generate-file-content [model-file]
  (let [model (util/model-from-file model-file)]
    (create-model-file model)
    (generate-migration-file model)
    (model-test-generator/generate-unit-test model)))

(defn
#^{:doc "Creates the model file associated with the given model."}
  generate-model-file [model]
    (if model
      (let [models-directory (util/find-models-directory)]
        (if models-directory
          (let [model-file (builder/create-model-file models-directory model)]
            (if model-file
              (generate-file-content model-file)))
          (logging/error (str "Could not find models directory: " models-directory))))
      (model-usage)))
        
(defn 
#^{:doc "Generates a model file for the model name in params."}
  generate [params]
  (generate-model-file (first params)))