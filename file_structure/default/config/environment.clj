(ns environment
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.util.loading-utils :as loading-utils]))
  
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

(def use-session-cookie true) ; Causes Conjure to save session ids as cookies. If this is false, Conjure uses a parameter in html.

(require 'conjure.util.session-utils) ; Avoids a circular dependency issue.
(def session-store conjure.util.session-utils/session-db-store)