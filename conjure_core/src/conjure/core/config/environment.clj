(ns conjure.core.config.environment
  (:require [config.environment :as config-env]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.logging :as logging]
            [clojure.tools.servlet-utils :as servlet-utils]))

(def initialized (atom false))

(defn
  find-config-env-value
  ([evironment-key] (find-config-env-value evironment-key nil))
  ([evironment-key default-value]
    (get (deref config-env/properties) evironment-key default-value)))

(def conjure-environment-property (find-config-env-value :conjure-environment-property "conjure.environment"))
(def default-environment (find-config-env-value :default-environment "development"))

(def source-dir (find-config-env-value :source-dir "src"))
(def servlet-source-dir (find-config-env-value :servlet-source-dir "classes")) 
(def test-dir (find-config-env-value :test-dir "test"))

(def assets-dir (find-config-env-value :assets-dir "public"))
(def javascripts-dir (find-config-env-value :javascripts-dir "javascripts"))
(def stylesheets-dir (find-config-env-value :stylesheets-dir "stylesheets"))
(def images-dir (find-config-env-value :images-dir "images"))

(def jquery (find-config-env-value :jquery "jquery-1.3.2.min.js"))
(def conjure-js (find-config-env-value :conjure-js "conjure.js"))

(def call-controller-fn (find-config-env-value :call-controller-fn)) 

(defn
  set-evironment-property [environment]
  (System/setProperty conjure-environment-property environment)) 

(defn
#^{ :doc "Returns the name of the environment." }
  environment-name []
  (System/getProperty conjure-environment-property))

(defn
  require-environment []
  (when (not (environment-name))
    (set-evironment-property default-environment))
  (let [mode (environment-name)]
    (require (symbol (str "config.environments." mode)))
    (logging/info (str "Environment Mode: " mode))))

(defn
  find-dir [directory-name]
  (or
    (loading-utils/get-classpath-dir-ending-with directory-name)
    (file-utils/find-directory (file-utils/user-directory) directory-name)
    (servlet-utils/find-servlet-directory directory-name))) 

(defn
#^{ :doc "Returns the source file directory as a File object, if it can be found." }
  find-source-dir []
  (or
    (find-dir source-dir)
    (find-dir servlet-source-dir)))

(defn
#^{ :doc "Returns the test file directory as a File object, if it can be found." }
  find-test-dir []
  (find-dir test-dir)) 

(defn
#^{ :doc "Returns the given child directory of the source directory if it can be found." }
  find-in-source-dir [child-dir-name]
  (file-utils/find-directory (find-source-dir) child-dir-name)) 

(defn
#^{ :doc "Returns true if Conjure should reload files for every request." }
  reload-files? []
  (find-config-env-value :reload-files false))

(defn call-controller
  "Calls the controller function. If the controller function is not set, this function tries to load the conjure.core.controller.util/call-controller."
  []
  (if call-controller-fn
    (call-controller-fn)
    ((ns-resolve 'conjure.core.controller.util 'call-controller))))