(ns conjure.core.helper.util
  (:import [java.io File])
  (:require [clojure.contrib.str-utils :as contrib-str-utils]
            [conjure.core.config.environment :as environment]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.string-utils :as conjure-str-utils]))

(def helpers-dir "helpers")

(defn 
#^{ :doc "Finds the helpers directory." }
  find-helpers-directory []
  (environment/find-in-source-dir helpers-dir))

(defn
#^{ :doc "Returns all of the helper files in the helpers directory." }
  helper-files []
  (filter loading-utils/clj-file? (file-seq (find-helpers-directory))))

(defn
#^{ :doc "Returns the helper namespace for the given helper file." }
  helper-namespace 
  [helper-file]
  (loading-utils/file-namespace (.getParentFile (find-helpers-directory)) helper-file))

(defn
#^{ :doc "Returns true if the given namespace is a helper namespace. The given namespace can be an actual namespace or
the string name of the namespace." }
  is-helper-namespace? [namespace]
  (when namespace
    (if (string? namespace)
      (.startsWith namespace (str helpers-dir "."))
      (is-helper-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns all of the helper namespaces in the app." }
  all-helper-namespaces []
  (filter is-helper-namespace? (all-ns)))