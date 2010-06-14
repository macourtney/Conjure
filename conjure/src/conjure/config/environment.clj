(ns conjure.config.environment
  (:require [clojure.contrib.java-utils :as java-utils]
            [config.environment :as config-env]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(def initialized (atom false))

(defn
  find-config-env-value [var-symbol default-value]
  (let [evn-var (ns-resolve (find-ns 'config.environment) var-symbol)]
    (if evn-var
      (var-get evn-var)
      default-value)))

(def conjure-environment-property (find-config-env-value 'conjure-environment-property "conjure.environment"))
(def default-environment (find-config-env-value 'default-environment "development"))

(def source-dir (find-config-env-value 'source-dir "src"))

(defn
  require-environment []
  (let [initial-value (java-utils/get-system-property conjure-environment-property nil)]
    (if (not initial-value)
      (java-utils/set-system-properties { conjure-environment-property default-environment })))
  (require (symbol (str "config.environments." (java-utils/get-system-property conjure-environment-property nil))))) 

(defn
#^{ :doc "Returns the name of the environment." }
  environment-name []
  (java-utils/get-system-property conjure-environment-property nil))

(defn
#^{ :doc "Returns the source file directory as a File object, if it can be found." }
  find-source-dir []
  (or
    (loading-utils/get-classpath-dir-ending-with config-env/source-dir)
    (file-utils/find-directory (file-utils/user-directory) config-env/source-dir)))

(defn
#^{ :doc "Returns the given child directory of the source directory if it can be found." }
  find-in-source-dir [child-dir-name]
  (file-utils/find-directory (find-source-dir) child-dir-name)) 

(defn
#^{ :doc "Returns true if Conjure should reload files for every request." }
  reload-files? []
  config-env/reload-files) 

(require-environment)