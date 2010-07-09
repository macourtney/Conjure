(ns conjure.script.test-plugin
  (:use clojure.contrib.test-is
        conjure.script.plugin)
  (:require [conjure.core.plugin.util :as plugin-util]
            [conjure.script.destroyers.plugin-destroyer :as plugin-destroyer]
            [conjure.script.generators.plugin-generator :as plugin-generator]))

(def plugin-name "test")

(defn setup-all [function]
  (plugin-generator/generate-plugin-files { :name plugin-name, :silent true })
  (function)
  (plugin-destroyer/destroy-plugin plugin-name true))
        
(use-fixtures :once setup-all)

(deftest test-plugin-dirs
  (is (plugin-util/find-plugins-directory))
  (is (plugin-util/plugin-directory plugin-name)))

(deftest test-install
  (install plugin-name []))

(deftest test-uninstall
  (uninstall plugin-name []))