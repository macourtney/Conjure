(ns conjure.extract-test
  (:import [java.io File])
  (:use [conjure.extract] :reload-all)
  (:use [clojure.test])
  (:require [clojure_util.file-utils :as file-utils]))

(deftest test--main
  (-main "test-project")
  (file-utils/recursive-delete (File. "test_project")))
