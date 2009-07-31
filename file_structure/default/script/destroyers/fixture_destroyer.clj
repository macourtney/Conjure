(ns destroyers.fixture-destroyer
  (:require [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the destroy fixture command."}
  usage []
  (println "You must supply a model (Like message).")
  (println "Usage: ./run.sh script/destroy.clj fixture <model>"))

(defn
#^{:doc "Destroys the fixture from the given model."}
  destroy-fixture-file [model]
  (if model
    (let [fixture-dir (util/find-fixture-directory)]
      (if fixture-dir
        (let [fixture-file (util/fixture-file model fixture-dir)]
          (if fixture-file
            (let [is-deleted (. fixture-file delete)] 
              (println "File" (. fixture-file getName) (if is-deleted "destroyed." "not destroyed."))
              (file-utils/delete-all-if-empty fixture-dir))
            (println "Fixture file not found. Doing nothing.")))
        (do
          (println "Could not find the fixture directory.")
          (println "Command ignored."))))
    (usage)))

(defn
#^{:doc "Destroys a fixture file for the model given in params."}
  destroy-fixture [params]
  (destroy-fixture-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the model_test_generator."}
  destroy-all-dependencies [model]
    (destroy-fixture-file model))