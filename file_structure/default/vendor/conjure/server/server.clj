(ns conjure.server.server
  (:use [http-config]
        [db-config]
        [conjure.server.jdbc-connector :as jdbc-connector]
        [conjure.util.loading-utils :as loading-utils]
        [clojure.contrib.str-utils :as str-utils]
        [routes :as routes]))


;; Gets a route map for use by conjure to call the correct methods.
(defn create-request-map [path]
  (let [routes-vector (routes/draw)]
    (loop [index 0] 
      (let [route-fn (nth routes-vector index)
           output (route-fn path)]
           
        (if output
          output
          (recur (inc index) ))))))

(defn controller-file-name [request-map]
  (str (:controller request-map) "_controller.clj"))
  
(defn fully-qualified-action [request-map]
  (str "controllers." (str-utils/re-gsub (re-pattern "_") "-" (:controller request-map)) "-controller/" (:action request-map)))

(defn load-controller [filename]
  (loading-utils/load-resource "controllers" filename))

;; Takes the given path and calls the correct controller and action for it.
(defn process-request [path]
  (let [request-map (create-request-map path)]
    (load-controller (controller-file-name request-map))
    ((load-string (fully-qualified-action request-map)) request-map)))
    
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