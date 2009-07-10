(ns test.model.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.model.builder)
  (:require [conjure.model.util :as util]))

(def model-name "test")

(deftest test-create-model-file
  (let [model-file-name (str model-name ".clj")]
    (let [model-file (create-model-file (util/find-models-directory) model-name)]
      (test-file model-file model-file-name)
      (. model-file delete))
    (let [model-file (create-model-file model-name)]
      (test-file model-file model-file-name)
      (. model-file delete)))
  (is (nil? (create-model-file nil)))
  (is (nil? (create-model-file (util/find-models-directory) nil)))
  (is (nil? (create-model-file nil model-name)))
  (is (nil? (create-model-file nil nil))))