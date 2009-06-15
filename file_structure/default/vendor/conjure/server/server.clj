(ns conjure.server.server
  (:use [http-config]
        [db-config]
        [conjure.server.jdbc-connector :as jdbc-connector]
        [conjure.util.loading-utils :as loading-utils]
        [clojure.contrib.str-utils :as str-utils]
        [routes :as routes]))

(defn
#^{:doc "Gets a route map for use by conjure to call the correct methods."}
  create-request-map [path params]
  (let [routes-vector (routes/draw)]
    (loop [current-routes routes-vector]
      (if (seq current-routes)
        (let [route-fn (first current-routes)
             output (route-fn path)]
          (if output
            (let [output-params (output :params)]
              (if output-params
                  (assoc output :params (merge output-params params))
                  output))
            (recur (rest current-routes))))))))

(defn controller-file-name [request-map]
  (str (:controller request-map) "_controller.clj"))
  
(defn fully-qualified-action [request-map]
  (str "controllers." (loading-utils/underscores-to-dashes (:controller request-map)) "-controller/" (:action request-map)))

(defn load-controller [filename]
  (loading-utils/load-resource "controllers" filename))

(defn
#^{:doc "Takes the given path and calls the correct controller and action for it."}
  process-request [path params]
  (let [request-map (create-request-map path params)]
    (when request-map
      (load-controller (controller-file-name request-map))
      ((load-string (fully-qualified-action request-map)) request-map))))
    
(defn load-view [filename]
  (loading-utils/load-resource-as-string "views" filename))

(defn view-file-path [params]
  (str (:controller params) "/" (:action params) ".clj"))

(defmacro
#^{:doc "A macro for simplifying the loading of views."}
  render-view 
  [params expr]
  `(let [execute# (load-string (str "(fn " '~expr " " (load-view (str (view-file-path ~params))) ")" ))]
      (execute# ~@expr)))

(defn
#^{:doc "Gets the user configured http properties."}
  http-config []
  (http-config/get-http-config))

(defn db-config []
  (db-config/get-db-config))

(defn
#^{:doc "This is the first method called when the server is started."}
  config-server []
  (http-config)
  (jdbc-connector/init))