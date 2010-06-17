(ns conjure.core.model.util
  (:require [clj-record.util :as clj-record-util]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as contrib-str-utils]
            [conjure.core.config.environment :as environment]
            [conjure.core.util.loading-utils :as loading-utils]
            [conjure.core.util.file-utils :as file-utils]
            [conjure.core.util.string-utils :as string-utils]))

(def models-dir "models")

(defn
#^{ :doc "Returns the model name for the given model file." }
  model-from-file [model-file]
  (if model-file
    (loading-utils/clj-file-to-symbol-string (. model-file getName))))
  
(defn
#^{ :doc "Returns the model namespace for the given model." }
  model-namespace [model]
  (if model (str "models." model)))
  
(defn
#^{ :doc "Loads the namespace for the given model." }
  load-model [model]
  (require (symbol (model-namespace model))))

(defn
#^{ :doc "Runs the given function in the given model with the given parameters." }
  run-model-fn [model function & params]
  (load-model model)
  (apply (ns-resolve (find-ns (symbol (model-namespace model))) (symbol function)) params))

(defn 
#^{ :doc "Finds the models directory." }
  find-models-directory []
  (environment/find-in-source-dir models-dir))

(comment  
  (defn
  #^{ :doc "Returns all of the model files in the models directory." }
    model-files []
    (filter loading-utils/clj-file? (file-seq (find-models-directory))))
  
  (defn
  #^{ :doc "Returns the model namespace for the given model file." }
    model-file-namespace 
    [model-file]
    (loading-utils/file-namespace (.getParentFile (find-models-directory)) model-file)))

(defn
  model-namespace? [namespace]
  (when namespace
    (if (string? namespace)
      (.startsWith namespace (str models-dir "."))
      (model-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns a sequence of all model namespaces." }
  all-model-namespaces []
  (filter model-namespace? (all-ns)))

(defn
#^{ :doc "Returns the model file name for the given model name." }
  model-file-name-string [model-name]
  (if model-name (str (loading-utils/dashes-to-underscores model-name) ".clj")))
  
(defn
#^{ :doc "Returns the name of the migration associated with the given model." }
  migration-for-model [model]
  (if model (str "create-" (clj-record-util/pluralize model))))
  
(defn
#^{ :doc "Returns the table name for the given model." }
  model-to-table-name [model]
  (if model (clj-record-util/pluralize (loading-utils/dashes-to-underscores model))))

(comment
  (defn
  #^{ :doc "Finds a model file with the given model name." }
    find-model-file
    ([model-name] (find-model-file (find-models-directory) model-name)) 
    ([models-directory model-name]
      (if (and models-directory model-name)
        (file-utils/find-file models-directory (model-file-name-string model-name)))))
)

(defn
#^{ :doc "Returns the model name for the given belongs to column." }
  to-model-name [belongs-to-column]
  (string-utils/strip-ending belongs-to-column "-id"))