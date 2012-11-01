(ns conjure.util.memory-session-store
  (:import [java.util Date])
  (:require [conjure.util.session-utils :as session-utils]))

(def data-store (ref {}))

(def created-at-key :created_at)
(def data-key :data)

(defn
#^{ :doc "No initialization is needed." }
  init [])

(defn
#^{ :doc "Creates a row in the database for a new session and returns the session id. If a value is given, it is saved 
in the database." }
  create-session 
  ([key-name value] (create-session key-name value (session-utils/session-id)))
  ([key-name value session-id]
    (dosync
      (alter data-store assoc session-id 
        { created-at-key (new Date), data-key { key-name value } }))))

(defn
#^{ :doc "Deletes the row in the database for the given session-id, or the session id from the request-map." }
  drop-session 
  ([] (drop-session (session-utils/session-id)))
  ([session-id]
    (dosync
      (alter data-store dissoc (session-utils/session-id)))))

(defn
#^{ :doc "Retrieves the value stored in the database for the given session id or the id in the request-map." }
  retrieve 
  ([] (retrieve (session-utils/session-id)))
  ([session-id]
    (data-key (get @data-store session-id))))

(defn-
#^{ :doc "Replaces the map stored in the session table with the given store-map." }
  save-map 
  ([data-map] (save-map data-map (session-utils/session-id)))
  ([data-map session-id] (save-map data-map session-id (retrieve session-id)))
  ([data-map session-id stored-map]
    (alter data-store assoc session-id 
      (assoc stored-map data-key data-map))))

(defn
#^{ :doc "Deletes the given key-name from the session store." }
  delete [key-name]
  (dosync
    (let [session-id (session-utils/session-id)
          stored-map (retrieve session-id)]
      (when stored-map
        (save-map (dissoc stored-map key-name) session-id stored-map)))))

(defn
#^{ :doc "Stores the given value in the session from the request-map." }
  save [key-name value]
  (dosync
    (let [session-id (session-utils/session-id)
          stored-map (retrieve session-id)]
      (if stored-map
        (save-map (assoc stored-map key-name value) session-id stored-map)
        (create-session key-name value session-id)))))

(def session-store 
  { :init init, 
    :drop drop-session,
    :delete delete,
    :save save,
    :retrieve retrieve })