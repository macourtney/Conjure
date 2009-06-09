(ns conjure.model.model
  (:import [java.io File])
  (:require [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.string-utils :as string-utils]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Returns the model name for the given model file."}
  model-from-file [model-file]
  (loading-utils/clj-file-to-symbol-string (. model-file getName)))
  
(defn
#^{:doc "Returns the model namespace for the given model."}
  model-namespace [model]
  (str "models." model))
  
(defn 
#^{:doc "Finds the models directory."}
  find-models-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "models"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
    
(defn
#^{:doc "Returns the model file name for the given model name."}
  model-file-name-string [model-name]
  (str (loading-utils/dashes-to-underscores model-name) ".clj"))
    
(defn
#^{:doc "Creates a new model file from the given model name."}
  create-model-file [models-directory model-name]
  (let [model-file (new File models-directory (model-file-name-string model-name))]
    (if (. model-file exists)
      (println (. model-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating model file" (. model-file getName) "...")
        (. model-file createNewFile)
        model-file))))
        
(defn
#^{:doc "Returns the name of the migration associated with the given model."}
  migration-for-model [model]
  (str "create-" (string-utils/pluralize model)))
  
(defn
#^{:doc "Finds a model file with the given model name."}
  find-model-file [models-directory model-name]
  (file-utils/find-file models-directory (model-file-name-string model-name)))