(ns leiningen.conjure_test
  (:use [leiningen.conjure] :reload-all)
  (:use [clojure.test]))

(deftest test-conjure
  (conjure 
    { :name "conjure-test"
      :version "1.0.0"
      :library-path "lib/"
      :compile-path "classes/"
      :native-path "src/native" }
    "noop"))