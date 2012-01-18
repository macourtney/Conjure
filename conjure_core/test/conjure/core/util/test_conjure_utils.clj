(ns conjure.core.util.test-conjure-utils
  (:import [java.io File])
  (:use clojure.test
        conjure.core.util.conjure-utils)
  (:require [controllers.home-controller :as home-controller]))

(def home-controller-file (.getAbsoluteFile (File. "test/controllers/home_controller.clj")))
(def home-controller-namespace-map (create-file-namespace-map home-controller-file))
(def home-controller-namespace-info (namespace-info home-controller-namespace-map))

(deftest test-conjure-namespaces
  (is (= #{"helpers.home-helper"} (conjure-namespaces 'controllers.home-controller))))

(deftest test-file-namespaces
  (is (file-namespaces)))

(deftest test-filter-file-namespaces
  (is (= [home-controller-namespace-map]
         (filter-file-namespaces ["controllers.home-controller"])))
  (is (= [] (filter-file-namespaces ["clojure.xml"])))
  (is (= [home-controller-namespace-map]
         (filter-file-namespaces ["clojure.xml" "controllers.home-controller"])))
  (is (= [] (filter-file-namespaces [])))
  (is (= [] (filter-file-namespaces nil))))

(deftest test-loaded-namespaces
  (clear-loaded-namespaces)
  (is (not (namespace-loaded? "controllers.home-controller")))
  (is (nil? (namespace-load-info "controllers.home-controller")))
  (add-namespace-info (create-file-namespace-map home-controller-file))
  (is (namespace-loaded? "controllers.home-controller"))
  (is (= home-controller-namespace-info (namespace-load-info "controllers.home-controller")))
  (is (not (reload-namespace? "controllers.home-controller")))
  (is (not (reload-namespace-map? home-controller-namespace-map)))
  (is (= [] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (clear-loaded-namespaces)
  (is (not (reload-namespace? "controllers.home-controller")))
  (is (not (reload-namespace-map? home-controller-namespace-map)))
  (is (= [] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (add-namespace-info
    (assoc home-controller-namespace-map :last-modified (- (.lastModified home-controller-file) 10000)))
  (is (reload-namespace? "controllers.home-controller"))
  (is (reload-namespace-map? home-controller-namespace-map))
  (is (= [home-controller-namespace-map] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (clear-loaded-namespaces))

(deftest test-reload-conjure-namespaces
  (clear-loaded-namespaces)
  (reload-conjure-namespaces "controllers.home-controller")
  (is (namespace-loaded? "controllers.home-controller"))
  (is (namespace-loaded? "helpers.home-helper"))
  (clear-loaded-namespaces))