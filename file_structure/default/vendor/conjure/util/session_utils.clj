(ns conjure.util.session-utils
  (:import [java.util Calendar Date])
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.string-utils :as conjure-str-utils]))

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
  temp-session-id [request-map]
  (:temp-session request-map))

(defn
#^{ :doc "Returns the session id from the given request map." }
  session-id [request-map]
  (let [params (:params request-map)
        params-session-id (or (if params (:session-id params)) (temp-session-id request-map))]
    (if params-session-id
      params-session-id
      (let [headers (:headers (:request request-map))]
        (get
          (conjure-str-utils/str-to-map 
            (if headers (get headers "cookie") ""))
          session-id-name)))))

(defn
#^{ :doc "Creates a temp session for the given request-map if one does not already exist." }
  update-request-session [request-map]
  (if (not (session-id request-map))
    (merge request-map { :temp-session (create-session-id) })
    request-map))
          
(defn
#^{ :doc "Returns true if a session has already been created." }
  session-created? [request-map response-map]
  (or 
    (get (:headers (:request request-map)) "cookie")
    (:session-id (:params request-map))
    (get (:headers response-map) "Set-Cookie")))

(defn
#^{ :doc "Updates the response map with a session cookie if necessary." }
  manage-session [request-map response-map]
  (if (not (session-created? request-map response-map))
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
                (or (temp-session-id request-map) (create-session-id))
                "; expires=" 
                (html-utils/format-cookie-date (. tomorrow getTime))
                "; path=/") })))
    response-map))