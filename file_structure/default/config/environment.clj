(ns environment
  (:require [clojure.contrib.java-utils :as java-utils]))
  
(defn conjure-environment-property "conjure.environment")

(def assets-dir "public")
(def javascripts-dir "javascripts")
(def stylesheets-dir "stylesheets")
(def images-dir "images")

(defn
#^{:doc "Initializes the environment."}
  init []
  (let [initial-value (java-utils/get-system-property conjure-environment-property nil)]
    (if (not initial-value)
      (java-utils/set-system-properties { conjure-environment-property "development" }))))

(defn
#^{:doc "Returns the name of the environment."}
  environment-name []
  (do
    (init)
    (java-utils/get-system-property conjure-environment-property nil)))