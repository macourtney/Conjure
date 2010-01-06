(ns conjure.util.session-utils
  (:import [java.util Calendar Date])
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.model.database :as database]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [environment :as environment]))

(def session-id-name "SID")
(def session-table :sessions)
(def created-at-column :created_at)
(def session-id-column :session_id)
(def data-column :data)

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
      (let [headers (:headers request-map)]
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
    (get (:headers request-map) "cookie")
    (:session-id (:params request-map))
    (get (:headers response-map) "Set-Cookie")))

(defn
#^{ :doc "Updates the response map with a session cookie if necessary." }
  manage-session [request-map response-map]
  (if (and environment/use-session-cookie (not (session-created? request-map response-map)))
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

(defn
#^{ :doc "Makes sure the session table exists in the database. If it doesn't exist, then this method creates it." }
  init []
  (if (not (database/table-exists? session-table))
    (database/create-table session-table
      (database/id)
      (database/date-time created-at-column)
      (database/string session-id-column)
      (database/text data-column))))

(defn
#^{ :doc "Creates a row in the database for a new session and returns the session id. If a value is given, it is saved 
in the database." }
  create-session 
  ([request-map value]
    (database/insert-into session-table 
      { created-at-column (database/format-date-time (new Date)), 
        session-id-column (session-id request-map), 
        data-column (conjure-str-utils/form-str value) })))
      
(defn
#^{ :doc "Deletes the row in the database for the given session-id, or the session id from the given request-map." }
  delete-session 
  ([request-map] (delete-session request-map (session-id request-map)))
  ([request-map session-id]
    (database/delete session-table [ (str (conjure-str-utils/str-keyword session-id-column) " = ?") session-id ])))
      
(defn-
#^{ :doc "Returns the id of the row for the session in the given request-map or session-id." }
  retrieve-session-row-id 
  ([request-map] (retrieve-session-row-id request-map (session-id request-map)))
  ([request-map session-id]
    (let [ row-values (database/sql-find 
          { :table session-table, 
            :select "id", 
            :where (str (conjure-str-utils/str-keyword session-id-column) " = '" session-id "'")})]
      (if row-values
        (:id (first row-values))))))

(defn
#^{ :doc "Retrieves the value stored in the database for the given session id or the id in the given request-map." }
  retrieve-store 
  ([request-map] (retrieve-store request-map (session-id request-map)))
  ([request-map session-id]
    (let [ row-values (database/sql-find 
          { :table session-table, 
            :select (conjure-str-utils/str-keyword data-column), 
            :where (str (conjure-str-utils/str-keyword session-id-column) " = '" session-id "'")})]
      (if row-values
        (let [data (get (first row-values) data-column)]
          (if data
            (read-string data)))))))

(defn
#^{ :doc "Stores the given value in the session in the given request-map." }
  save-store [request-map value] 
  (let [ row-id (retrieve-session-row-id request-map)]
    (if row-id
      (database/update 
        session-table 
        ["id = ?" row-id] 
        { data-column (conjure-str-utils/form-str value) })
      (create-session request-map value))))

(def session-db-store 
  { :init init, 
    :delete delete-session,
    :store save-store,
    :retrieve retrieve-store })