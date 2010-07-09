(ns conjure.core.plugin.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.core.plugin.util
        test-helper)
  (:require [clojure.contrib.logging :as logging]))

(def plugin-name "test")

(use-fixtures :once init-server)

(deftest test-find-plugins-directory
  (let [plugins-directory (find-plugins-directory)]
    (is plugins-directory)
    (is (instance? File plugins-directory))
    (when plugins-directory
      (is (.exists plugins-directory)))))

(deftest test-plugin-namespace-name
  (is (plugin-namespace-name "test"))
  (is (= (plugin-namespace-name "test-plugin") (plugin-namespace-name "test_plugin"))))

(deftest test-plugin-ns
  (is (plugin-ns plugin-name))
  (is (not (plugin-ns "fail"))))

(deftest test-plugin-name-from-namespace
  (is (= "test-plugin" (plugin-name-from-namespace "plugins.test-plugin.plugin"))))

(deftest test-install-fn
  (is (install-fn plugin-name))
  (is (not (install-fn "fail"))))

(deftest test-uninstall-fn
  (is (uninstall-fn plugin-name))
  (is (not (uninstall-fn "fail"))))

(deftest test-initialize-fn
  (is (initialize-fn plugin-name))
  (is (not (initialize-fn "fail"))))

(deftest test-plugin-directory
  (let [test-plugin-directory (plugin-directory "test")]
    (is (not (nil? test-plugin-directory)))
    (is (instance? File test-plugin-directory))))

(deftest test-all-plugins
  (is (= ["test"] (all-plugins))))

(deftest test-all-initialize-fns
  (let [initialize-fns (all-initialize-fns)]
    (is initialize-fns)
    (is (= 1 (count initialize-fns)))))

(deftest test-initialize-all-plugins
  (initialize-all-plugins))

(deftest test-plugin-file-namespace
  (is (= (symbol (str "plugins." plugin-name ".plugin")) 
    (plugin-file-namespace (File. (plugin-directory plugin-name) "plugin.clj")))))

(deftest test-test-namespace-name
  (is (= (str "plugins." plugin-name ".test-plugin") (test-namespace-name plugin-name))))