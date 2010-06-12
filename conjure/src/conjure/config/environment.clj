(ns conjure.config.environment
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.util.loading-utils :as loading-utils]))
  
(def default-conjure-environment-property "conjure.environment")
(def default-environment "development")

(def default-assets-dir "public")
(def default-javascripts-dir "javascripts")
(def default-stylesheets-dir "stylesheets")
(def default-images-dir "images")

(def default-jquery "jquery-1.3.2.min.js")
(def default-conjure-js "conjure.js")

(def reload-files true)

(def initialized (atom false))

(defn
#^{ :doc "Returns the value of the given var symbol in the environment namespace or default if the var or the namespace
cannot be found.." }
  resolve-environment-var [var-sym default]
  (loading-utils/resolve-ns-var 'config.environment var-sym default))
  
(defn
#^{ :doc "Returns the java system property name of the environment." }
  conjure-environment-property []
  ((resolve-environment-var 'conjure-environment-property default-conjure-environment-property)))

(defn
#^{ :doc "Initializes the environment." }
  default-init []
  (when (not @initialized)
    (println "Initializing environment...")
    (reset! initialized true)
    (let [initial-value (java-utils/get-system-property (conjure-environment-property) nil)]
      (if (not initial-value)
        (java-utils/set-system-properties { (conjure-environment-property) default-environment })))
    (loading-utils/load-resource "environments" (str (java-utils/get-system-property (conjure-environment-property) nil) ".clj"))))

(defn
  init []
  (resolve-environment-var 'init default-init))

(defn
#^{ :doc "Returns the default name of the environment." }
  default-environment-name []
  (do
    (init)
    (java-utils/get-system-property (conjure-environment-property) nil)))

(defn
  reload-files? []
  (resolve-environment-var 'reload-files reload-files))
  
(defn
#^{ :doc "Returns the name of the environment." }
  environment-name []
  ((resolve-environment-var 'environment-name default-environment-name)))

(defn
#^{ :doc "Returns the assets directory." }
  assets-dir []
  ((resolve-environment-var 'assets-dir default-assets-dir)))

(defn
#^{ :doc "Returns the javascripts directory." }
  javascripts-dir []
  ((resolve-environment-var 'javascripts-dir default-javascripts-dir)))

(defn
#^{ :doc "Returns the stylesheets directory." }
  stylesheets-dir []
  ((resolve-environment-var 'stylesheets-dir default-stylesheets-dir)))

(defn
#^{ :doc "Returns the images directory." }
  images-dir []
  ((resolve-environment-var 'images-dir default-images-dir)))

(defn
#^{ :doc "Returns the name of the jquery script to use." }
  jquery []
  ((resolve-environment-var 'jquery default-jquery)))

(defn
#^{ :doc "Returns the name of the conjure script to use." }
  conjure-js []
  ((resolve-environment-var 'conjure-js default-conjure-js)))