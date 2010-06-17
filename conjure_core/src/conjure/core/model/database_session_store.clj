(ns conjure.core.model.database-session-store
  (:import [java.util Calendar Date])
  (:require [conjure.core.model.database :as database]
            [conjure.core.util.session-utils :as session-utils]
            [conjure.core.util.string-utils :as conjure-str-utils]))

(def session-table :sessions)
(def created-at-column :created_at)
(def session-id-column :session_id)
(def data-column :data)

(defn
#^{ :doc "Makes sure the session table exists in the database. If it doesn't exist, then this method creates it." }
  init []
  (when (not (database/table-exists? session-table))
    (database/create-table session-table
      (database/id)
      (database/date-time created-at-column)
      (database/string session-id-column)
      (database/text data-column))))

(defn
#^{ :doc "Creates a row in the database for a new session and returns the session id. If a value is given, it is saved 
in the database." }
  create-session
  ([key-name value]
    (database/insert-into session-table 
      { created-at-column (database/format-date-time (new Date)), 
        session-id-column (session-utils/session-id), 
        data-column (conjure-str-utils/form-str { key-name value }) })))
      
(defn
#^{ :doc "Deletes the row in the database for the given session-id, or the session id from the request-map." }
  drop-session 
  ([] (drop-session (session-utils/session-id)))
  ([session-id]
    (database/delete session-table [ (str (conjure-str-utils/str-keyword session-id-column) " = ?") session-id ])))

(defn-
#^{ :doc "Replaces the map stored in the session table with the given store-map." }
  save-map [store-map]
  (database/update 
    session-table 
    [(str (conjure-str-utils/str-keyword session-id-column) " = ?") (session-utils/session-id)] 
    { data-column (conjure-str-utils/form-str store-map) }))

(defn
#^{ :doc "Retrieves the value stored in the database for the given session id or the id in the request-map." }
  retrieve 
  ([] (retrieve (session-utils/session-id)))
  ([session-id]
    (when-let [row-values (database/sql-find 
                            { :table (conjure-str-utils/str-keyword session-table), 
                              :select (conjure-str-utils/str-keyword data-column), 
                              :where (str (conjure-str-utils/str-keyword session-id-column) " = '" session-id "'")  })]
      (when-let [data (get (first row-values) data-column)]
        (read-string data)))))

(defn
#^{ :doc "Deletes the given key-name from the session store." }
  delete [key-name]
  (let [ stored-map (retrieve)]
    (if stored-map
      (save-map (dissoc stored-map key-name)))))

(defn
#^{ :doc "Stores the given value in the session from the request-map." }
  save [key-name value]
  (let [stored-map (retrieve)]
    (if stored-map
      (save-map (assoc stored-map key-name value))
      (create-session key-name value))))

(def session-store 
  { :init init, 
    :drop drop-session,
    :delete delete,
    :save save,
    :retrieve retrieve })