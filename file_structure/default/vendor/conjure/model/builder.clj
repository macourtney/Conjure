(ns conjure.model.builder
  (:import [java.io File])
  (:require [conjure.model.util :as util]))

(defn
#^{:doc "Creates a new model file from the given model name."}
  create-model-file [models-directory model-name]
  (let [model-file (new File models-directory (util/model-file-name-string model-name))]
    (if (. model-file exists)
      (println (. model-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating model file" (. model-file getName) "...")
        (. model-file createNewFile)
        model-file))))