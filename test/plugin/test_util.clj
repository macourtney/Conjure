(ns test.plugin.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.plugin.util))

(defn setup-all [function]
  (let [generator-map {}]
    ;(controller-generator/generate-controller-file generator-map)
    ;(load-controller controller-name)
    (function)
    ;(controller-destroyer/destroy-all-dependencies generator-map)
    ;(reset! action-interceptors {})
    ))
        
(use-fixtures :once setup-all)
  
(deftest test-find-plugins-directory
  (let [plugins-directory (find-plugins-directory)]
    (is (not (nil? plugins-directory)))
    (is (instance? File plugins-directory))
    (is (.exists plugins-directory))))

(deftest test-plugin-namespace-name
  (is (plugin-namespace-name "test"))
  (is (= (plugin-namespace-name "test-plugin") (plugin-namespace-name "test_plugin"))))

(deftest test-plugin-directory
  (let [test-plugin-directory (plugin-directory "test")]
    (is (not (nil? test-plugin-directory)))
    (is (instance? File test-plugin-directory))))