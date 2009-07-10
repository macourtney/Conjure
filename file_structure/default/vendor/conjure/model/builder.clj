(ns conjure.model.builder
  (:import [java.io File])
  (:require [conjure.model.util :as util]))

(defn
#^{:doc "Creates a new model file from the given model name."}
  create-model-file
  ([model-name] (create-model-file (util/find-models-directory) model-name))
  ([models-directory model-name]
    (if (and models-directory model-name)
      (let [model-file (new File models-directory (util/model-file-name-string model-name))]
        (if (. model-file exists)
          (do
            (println (. model-file getName) "already exits. Doing nothing.")
            model-file)
          (do
            (println "Creating model file" (. model-file getName) "...")
            (. model-file createNewFile)
            model-file))))))