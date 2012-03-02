(ns conjure.util.session-utils
  (:import [java.util Calendar Date])
  (:require [clojure.tools.html-utils :as html-utils]
            [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.util.request :as request]))

(def session-id-name "SID")

(defn
#^{ :doc "Creates a session id for use in a session cookie or session id parameter." }
  create-session-id []
  (conjure-str-utils/md5-sum 
    "Conjure" 
    (str (. (new Date) getTime)) 
    (str (. Math random))))

(defn
#^{ :doc "Returns the temp session id from the given request map if it exists." }
  temp-session-id 
  ([] (temp-session-id request/request-map))
  ([request-map]
    (:temp-session request-map)))

(defn
#^{ :doc "Returns the session id from the given request map." }
  session-id 
  ([] (session-id request/request-map))
  ([request-map]
    (let [params (:params request-map)
          params-session-id (or (if params (:session-id params)) (temp-session-id request-map))]
      (if params-session-id
        params-session-id
        (let [headers (:headers (:request request-map))]
          (get
            (conjure-str-utils/str-to-map 
              (if headers (get headers "cookie") ""))
            session-id-name))))))

(defn
#^{ :doc "Creates a temp session for the given request-map if one does not already exist." }
  update-request-session [request-map]
  (if (not (session-id request-map))
    (assoc request-map :temp-session (create-session-id))
    request-map))

(defmacro
#^{ :doc "Adds a temp session to the request-map for body." }
  with-request-session [& body]
  `(request/with-request-map-fn update-request-session ~@body))

(defn
#^{ :doc "Returns true if a session has already been created." }
  session-created? [response-map]
  (or 
    (get (request/headers) "cookie")
    (:session-id (request/parameters))
    (get (:headers response-map) "Set-Cookie")))

(defn
#^{ :doc "Updates the response map with a session cookie if necessary." }
  manage-session [response-map]
  (if (not (session-created? response-map))
    (let [tomorrow (doto (. Calendar getInstance)
                     (.add (. Calendar DATE) 1))]
      (assoc
        response-map 
        :headers 
          (merge 
            (:headers response-map)
            { "Set-Cookie"
              (str 
                session-id-name
                "=" 
                (or (temp-session-id) (create-session-id))
                "; expires=" 
                (html-utils/format-cookie-date (. tomorrow getTime))
                "; path=/") })))
    response-map))