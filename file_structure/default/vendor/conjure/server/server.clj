(ns conjure.server.server
  (:use [http-config]
        [db-config]
        [conjure.server.jdbc-connector :as jdbc-connector]
        [conjure.util.loading-utils :as loading-utils]
        [clojure.contrib.str-utils :as str-utils]))


(defn controller-file-name [controller]
  (str controller "_controller.clj"))
  
(defn fully-qualified-action [controller action]
  (str "(controllers." (str-utils/re-gsub (re-pattern "_") "-" controller) "-controller/" action ")"))

(defn load-controller [filename]
  (loading-utils/load-resource "controllers" filename))

(defn load-view [filename]
  (loading-utils/load-resource-as-string "views" filename))

;; A macro for simplifying the loading of views
(defmacro render-view 
  [view expr]
  `(let [execute# (load-string (str "(fn " '~expr " " (load-view (str ~view ".clj")) ")" ))]
      (execute# ~@expr)))

;; Gets the user configured http properties.
(defn http-config []
  (http-config/get-http-config))

(defn db-config []
  (db-config/get-db-config))

;; This is the first method called when the server is started.
(defn config-server []
  (http-config)
  (jdbc-connector/init))