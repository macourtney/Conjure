(ns conjure.util.session-utils
  (:import [java.util Calendar Date])
  (:require [conjure.util.html-utils :as html-utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [environment :as environment]))

(def session-id-name "SID")

(defn
#^{ :doc "Creates a session id for use in a session cookie or session id parameter." }
  create-session-id []
  (conjure-str-utils/md5-sum 
    "Conjure" 
    (str (. (new Date) getTime)) 
    (str (. Math random))))

(defn
#^{ :doc "Returns the session id from the given request map." }
  session-id [request-map]
  (let [params (:params request-map)
        params-session-id (if params (:session-id params))]
    (if params-session-id
      params-session-id
      (let [headers (:headers request-map)]
        (get
          (conjure-str-utils/str-to-map 
            (if headers (get headers "cookie") ""))
          session-id-name)))))

(defn
#^{ :doc "Updates the response map with a session cookie if necessary." }
  manage-session [request-map response-map]
  (if (and environment/use-session-cookie (not (or (get (:headers request-map) "cookie") (get (:headers response-map) "Set-Cookie"))))
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
                (create-session-id)
                "; expires=" 
                (html-utils/format-cookie-date (. tomorrow getTime))
                "; path=/") })))
    response-map))