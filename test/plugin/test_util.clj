(ns test.plugin.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.plugin.util)
  (:require [destroyers.plugin-destroyer :as plugin-destroyer]
            [generators.plugin-generator :as plugin-generator]))

(def plugin-name "test")

(defn setup-all [function]
  (plugin-generator/generate-plugin-files { :name plugin-name, :silent true })
  (function)
  (plugin-destroyer/destroy-plugin plugin-name true))
        
(use-fixtures :once setup-all)

(deftest test-find-plugins-directory
  (let [plugins-directory (find-plugins-directory)]
    (is (not (nil? plugins-directory)))
    (is (instance? File plugins-directory))
    (is (.exists plugins-directory))))

(deftest test-plugin-namespace-name
  (is (plugin-namespace-name "test"))
  (is (= (plugin-namespace-name "test-plugin") (plugin-namespace-name "test_plugin"))))

(deftest test-plugin-ns
  (is (plugin-ns plugin-name))
  (is (not (plugin-ns "fail"))))

(deftest test-install-fn
  (is (install-fn plugin-name))
  (is (not (install-fn "fail"))))

(deftest test-uninstall-fn
  (is (uninstall-fn plugin-name))
  (is (not (uninstall-fn "fail"))))

(deftest test-plugin-directory
  (let [test-plugin-directory (plugin-directory "test")]
    (is (not (nil? test-plugin-directory)))
    (is (instance? File test-plugin-directory))))