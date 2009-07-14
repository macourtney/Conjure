(ns test
  (:use lancet)
  (:require [clojure.contrib.str-utils :as str-utils]))

(def test-dir "test")
(def target "target")
(def test-app (str target "/test_app"))
(def file-structure "file_structure")
(def default (str file-structure "/default"))
(def args-string (if (not-empty *command-line-args*) (str-utils/str-join " " *command-line-args*) ""))

(copy { :todir test-app }
  (fileset { :dir default }))
(copy { :todir (str test-app "/test") }
  (fileset { :dir test-dir }))

(echo { :message "\nRunning Tests...\n\n"})

(java { :classname "clojure.main"
        :dir test-app
        :fork "true" }
  [:arg { :value "test/run_tests.clj" }]
  [:arg { :value args-string }]
  [:classpath  
    [:pathElement { :path (str test-app "/vendor") }]
    [:pathElement { :path (str test-app "/app") }]
    [:pathElement { :path (str test-app "/config") }]
    [:pathElement { :path (str test-app "/script") }]
    [:pathElement { :path (str test-app "/db") }]
    [:pathElement { :path (str test-app "/test") }]
    (fileset { :dir (str test-app "/lib")
               :includes "**/*.jar" })])