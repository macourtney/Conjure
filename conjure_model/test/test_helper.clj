(ns test-helper
  (:import [java.io File])
  (:use clojure.test)
  (:require [config.db-config :as db-config]
            [config.session-config :as session-config]
            [conjure.config.environment :as environment]
            [drift-db.core :as drift-db]))
  
(defn 
#^{:doc "Verifies the given file is not nil, is an instance of File, and has the given name."}
  test-file [file expected-file-name]
  (is (not (nil? file)))
  (is (instance? File file))
  (is (and file (= expected-file-name (.getName file)))))
  
(defn
#^{:doc "Simply calls test-file on the given directory and name."}
  test-directory [directory expected-directory-name]
  (test-file directory expected-directory-name))

(defn init-server [test-fn]
  (environment/set-evironment-property "test")
  (environment/require-environment)
  (drift-db/init-flavor (db-config/load-config))
  ((:init session-config/session-store))
  (test-fn))