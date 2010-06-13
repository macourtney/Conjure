(ns config.environment
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.util.loading-utils :as loading-utils]))
  
(def conjure-environment-property "conjure.environment")
(def default-environment "test")

(def source-dir "test") ; Set to test for the testing environment

(def assets-dir "test/public")
(def javascripts-dir "javascripts")
(def stylesheets-dir "stylesheets")
(def images-dir "images")

(def jquery "jquery-1.3.2.min.js")
(def conjure-js "conjure.js")

(def reload-files false)

(def initialized (atom false))

(defn
#^{ :doc "Initializes the environment." }
  init []
  (when (not @initialized)
    (println "Initializing environment...")
    (reset! initialized true)
    (let [initial-value (java-utils/get-system-property conjure-environment-property nil)]
      (if (not initial-value)
        (java-utils/set-system-properties { conjure-environment-property default-environment })))
    (require (symbol (str "config.environments." (java-utils/get-system-property conjure-environment-property nil))))))

(defn
#^{ :doc "Returns the name of the environment." }
  environment-name []
  (do
    (init)
    (java-utils/get-system-property conjure-environment-property nil)))