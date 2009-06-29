(ns environment
  (:require [clojure.contrib.java-utils :as java-utils]))
  
(defn conjure-environment-property "conjure.environment")

(defn
#^{:doc "Initializes the environment."}
  init []
  (let [initial-value (java-utils/get-system-property conjure-environment-property nil)]
    (if (not initial-value)
      (java-utils/set-system-properties { conjure-environment-property "development" }))))