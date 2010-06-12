(ns conjure.plugin.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.plugin.util)
  (:require [clojure.contrib.logging :as logging]
            ;[destroyers.plugin-destroyer :as plugin-destroyer]
            ;[generators.plugin-generator :as plugin-generator]
            ))

(def plugin-name "test")

(defn setup-all [function]
  ;(plugin-generator/generate-plugin-files { :name plugin-name, :silent true })
  (function)
  ;(plugin-destroyer/destroy-plugin plugin-name true)
  )
        
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

(deftest test-test-directory
  (let [test-dir (test-directory plugin-name)]
    (is test-dir)
    (is (instance? File test-dir)))
  (is (not (test-directory "fail"))))

(deftest test-test-namespace-name
  (is (= (str "plugins." plugin-name ".test.test-plugin") (test-namespace-name plugin-name "test-plugin"))))

(deftest test-test-files
  (let [test-file-seq (test-files plugin-name)]
    (is test-file-seq)
    (is (= 1 (count test-file-seq))))
  (is (empty? (test-files "fail"))))

(deftest test-load-test-file
  (load-test-file (File. (test-directory plugin-name) "test_plugin.clj")))

(deftest test-plugin-file-namespace
  (is (= (symbol (str "plugins." plugin-name ".test.test-plugin")) 
        (plugin-file-namespace (File. (test-directory plugin-name) "test_plugin.clj")))))

(deftest test-run-test-list
  (run-test-list [(str "plugins." plugin-name ".test.test-plugin")])
  (run-test-list []))

(deftest test-run-all-plugin-tests
  (run-all-plugin-tests plugin-name)
  (run-all-plugin-tests "fail"))

(deftest test-run-plugin-tests
  (run-plugin-tests plugin-name [(str "plugins." plugin-name ".test.test-plugin")])
  (run-plugin-tests plugin-name []))