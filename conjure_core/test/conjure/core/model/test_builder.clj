(ns conjure.core.model.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.core.model.builder)
  (:require [conjure.core.model.util :as util]
            [conjure.core.util.loading-utils :as loading-utils]))

(def model-name "builder-test")

(deftest test-create-model-file
  (let [model-file-name (str (loading-utils/dashes-to-underscores model-name) ".clj")]
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