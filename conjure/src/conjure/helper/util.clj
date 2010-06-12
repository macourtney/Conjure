(ns conjure.helper.util
  (:import [java.io File])
  (:require [clojure.contrib.str-utils :as contrib-str-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as conjure-str-utils]))


(defn 
#^{ :doc "Finds the models directory." }
  find-helpers-directory []
  (new File (loading-utils/get-classpath-dir-ending-with "app") "helpers"))

(defn
#^{ :doc "Returns all of the model files in the models directory." }
  helper-files []
  (filter loading-utils/clj-file? (file-seq (find-helpers-directory))))

(defn
#^{ :doc "Returns the model namespace for the given model file." }
  helper-namespace 
  [helper-file]
  (loading-utils/file-namespace (.getParentFile (find-helpers-directory)) helper-file))

(defn
#^{ :doc "Returns a sequence of all model namespaces." }
  all-helper-namespaces []
  (map #(symbol (helper-namespace %)) (helper-files)))