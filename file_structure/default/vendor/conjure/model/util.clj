(ns conjure.model.util
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.string-utils :as string-utils]
            [clj-record.util :as clj-record-util]))

(defn
#^{:doc "Returns the model name for the given model file."}
  model-from-file [model-file]
  (if model-file
    (loading-utils/clj-file-to-symbol-string (. model-file getName))))
  
(defn
#^{:doc "Returns the model namespace for the given model."}
  model-namespace [model]
  (if model (str "models." model)))
  
(defn 
#^{:doc "Finds the models directory."}
  find-models-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "models"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
    
(defn
#^{:doc "Returns the model file name for the given model name."}
  model-file-name-string [model-name]
  (if model-name (str (loading-utils/dashes-to-underscores model-name) ".clj")))
  
(defn
#^{:doc "Returns the name of the migration associated with the given model."}
  migration-for-model [model]
  (if model (str "create-" (clj-record-util/pluralize model))))
  
(defn
#^{:doc "Returns the table name for the given model."}
  model-to-table-name [model]
  (if model (clj-record-util/pluralize (loading-utils/dashes-to-underscores model))))

(defn
#^{:doc "Finds a model file with the given model name."}
  find-model-file
  ([model-name] (find-model-file (find-models-directory) model-name)) 
  ([models-directory model-name]
    (if (and models-directory model-name)
      (file-utils/find-file models-directory (model-file-name-string model-name)))))
      
(defn
#^{:doc "Returns the model name for the given belongs to column."}
  to-model-name [belongs-to-column]
    (string-utils/strip-ending belongs-to-column "-id"))