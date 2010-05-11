(ns test.scripts.test-plugin
  (:use clojure.contrib.test-is
        plugin)
  (:require [destroyers.plugin-destroyer :as plugin-destroyer]
            [generators.plugin-generator :as plugin-generator]))

(def plugin-name "test")

(defn setup-all [function]
  (plugin-generator/generate-plugin-files { :name plugin-name, :silent true })
  (function)
  (plugin-destroyer/destroy-plugin plugin-name true))
        
(use-fixtures :once setup-all)

(deftest test-install
  (install plugin-name []))

(deftest test-uninstall
  (uninstall plugin-name []))

(deftest test-test-plugin
  (test-plugin plugin-name []))