(ns destroyers.fixture-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.test.util :as util]
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
              (logging/info (str "File " (. fixture-file getName) (if is-deleted " destroyed." " not destroyed.")))
              (file-utils/delete-all-if-empty fixture-dir))
            (logging/info "Fixture file not found. Doing nothing.")))
        (do
          (logging/error "Could not find the fixture directory.")
          (logging/error "Command ignored."))))
    (usage)))

(defn
#^{:doc "Destroys a fixture file for the model given in params."}
  destroy [params]
  (destroy-fixture-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the model_test_generator."}
  destroy-all-dependencies [model]
    (destroy-fixture-file model))