(ns conjure.core.server.request
  (:require [conjure.core.util.loading-utils :as loading-utils] 
            [conjure.core.util.html-utils :as html-utils]
            [conjure.core.util.string-utils :as conjure-str-utils]))

(def request-map {})

(defn
#^{ :doc "Returns the controller." }
  controller []
  (:controller request-map))

(defn
#^{ :doc "Returns the action." }
  action []
  (:action request-map))

(defn
#^{ :doc "Returns the user or nil if it is not set." }
  user []
  (:user request-map))

(defn
#^{ :doc "Returns the password or nil if it is not set." }
  password []
  (:password request-map))

(defn
#^{ :doc "Returns the port or nil if it is not set." }
  port []
  (:port request-map))

(defn
#^{ :doc "Returns true if only the path should be used to link to a url." }
  only-path? []
  (:only-path request-map))

(defn
#^{ :doc "Returns the anchor or nil if it is not set." }
  anchor []
  (:anchor request-map))

(defn
#^{ :doc "Returns name or nil if it is not set." }
  form-name []
  (:name request-map))

(defn
#^{ :doc "Returns layout info or nil if it is not set." }
  layout-info []
  (:layout-info request-map)) 

(defn
#^{ :doc "Returns the parameters from the request map or nil if no parameters a set." }
  parameters []
  (:params request-map))

(defn
#^{ :doc "Returns the id or nil if it is not set." }
  id []
  (:id (parameters)))

(defn
#^{ :doc "Returns the id as a str." }
  id-str []
  (let [id (id)]
    (if (and id (map? id))
      (:id id)
      id)))

(defn
#^{ :doc "Returns the record or nil if it is not set." }
  record []
  (:record (parameters)))

(defn
#^{ :doc "Returns the original request map sent by ring." }
  ring-request []
  (:request request-map))

(defn
#^{ :doc "Returns the uri from the request." }
  uri []
  (:uri (ring-request)))

(defn
#^{ :doc "Returns the headers from the request." }
  headers []
  (:headers (ring-request)))

(defn
#^{ :doc "Returns the referrer from the request." }
  referrer []
  (:referrer (headers)))

(defn
#^{ :doc "Returns the request method from the request. For example: GET, POST, PUT, etc." }
  method []
  (:method (ring-request)))

(defn
#^{ :doc "Returns the name of the server. The server name is the one set in the root request-map. If it is not found, 
then the server name set in the ring-request is used." }
  server-name []
  (or (:server-name request-map) (:server-name (ring-request))))

(defn
#^{ :doc "Returns the scheme. The scheme is the one set in the root request-map. If it is not found, 
then the scheme set in the ring-request is used." }
  scheme []
  (let [map-scheme (or (:scheme request-map) (:scheme (ring-request)))]
    (if map-scheme
      (conjure-str-utils/str-keyword map-scheme)
      "http")))

(defn
#^{ :doc "Returns the server port or nil if it is not set." }
  server-port []
  (:server-port (ring-request)))

(defn
#^{ :doc "Returns the server port or nil if it is not set." }
  url-port []
  (let [port (port)]
    (if port
      port
      (let [server-port (server-port)]
        (when (and server-port (not (= server-port 80))) 
          server-port)))))

(defn
#^{ :doc "Merges the params value of the given request-map with params" }
  augment-params [request-map params]
  (if request-map 
    (if (and params (not-empty params))
      (assoc request-map :params (merge (:params request-map) params))
      request-map)))

(defn
#^{ :doc "Returns a parameter map generated from the post content." }
  parse-post-params [request-map]
  (let [request (:request request-map)
        content-type (:content-type request)]
    (if 
      (and 
        (= (:request-method request) :post)
        content-type
        (.startsWith content-type "application/x-www-form-urlencoded"))
  
      (html-utils/parse-query-params 
        (loading-utils/string-input-stream (:body request) (:content-length request)))
      {})))

(defn
#^{ :doc "Parses all of the params from the given request map." }
  parse-params [request-map]
  (merge (parse-post-params request-map) (html-utils/parse-query-params (:query-string (:request request-map)))))

(defn
#^{ :doc "Gets a route map for use by conjure to call the correct methods." }
  update-request-map [request-map] 
  (augment-params request-map (parse-params request-map)))

(defmacro
#^{ :doc "Resets the request map to the given request map in body." }
  set-request-map [new-request-map & body]
  `(binding [request-map ~new-request-map]
    ~@body))

(defmacro
  with-request-map-fn [function & body]
  `(set-request-map (~function request-map) ~@body))

(defn
#^{ :doc "Merges the two values together. If both values are maps then they are merged together using this function.
Otherwise, new-value is returned." }
  map-merge [old-value new-value]
  (if (map? new-value)
    (if (map? old-value)
      (merge-with map-merge old-value new-value)
      new-value)
    new-value))

(defn
#^{ :doc "Returns a new request map which is the merger of the given request map and the new map. All values in new map
override the values in request-map unless both the request map and new map values are maps, in which case they are
merged together with new map values always winning." }
  request-map-merge [request-map new-map]
  (merge-with map-merge request-map new-map))

(defmacro
  with-merged-request-map [new-map & body]
  `(set-request-map (request-map-merge request-map ~new-map) ~@body))

(defmacro
#^{ :doc "With the given incoming request map which is the raw request map from ring, update the request map using 
update-request-map and use the new request map in body." }
  with-updated-request-map [incoming-request-map & body]
  `(set-request-map (update-request-map ~incoming-request-map) ~@body))

(defn
#^{ :doc "Returns the request map with the given controller, action, and id added to it." }
  request-map-with [controller action id]
  (let [output-request-map (merge request-map { :controller controller, :action action })]
    (if id
      (assoc output-request-map :params (assoc (parameters) :id id))
      output-request-map))) 

(defmacro
#^{ :doc "Updates the request map with the given controller action and id in body. If id is nil, it is ignored." }
  with-controller-action [controller action & body]
  `(set-request-map (request-map-with ~controller ~action nil) ~@body))

(defmacro
#^{ :doc "Updates the request map with the given controller action and id in body. If id is nil, it is ignored." }
  with-controller-action-id [controller action id & body]
  `(set-request-map (request-map-with ~controller ~action ~id) ~@body))

