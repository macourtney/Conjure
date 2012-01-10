(ns conjure.core.model.google-app-engine-session-store
  (:import [java.util Calendar Date])
  (:require [clojure.tools.logging :as logging]
            [conjure.core.util.session-utils :as session-utils]
            [clojure.tools.string-utils :as conjure-str-utils])
  (:use appengine.datastore))

(def session-table :sessions)
(def created-at-column :created_at)
(def session-id-column :session_id)
(def data-column :data)

(defentity Conjure-session ()
  ((session-id :key identity)
   (created-at)
   (data)))

(defn
#^{ :doc "Not used with google app engine." }
  init [])

(defn
#^{ :doc "Creates a new session entity and saves it to the data store." }
  create-session
  ([key-name value]
    (save-entity
      (conjure-session
        { :session-id (session-utils/session-id),
          :created-at (new Date),
          :data (conjure-str-utils/form-str { key-name value }) }))))
      
(defn
#^{ :doc "Deletes the entity for the given session-id, or the session id from the request-map." }
  drop-session 
  ([] (drop-session (session-utils/session-id)))
  ([session-id]
    (delete-entity (find-entity { :session-id session-id }))))

(defn-
#^{ :doc "Replaces the map stored in the data store with the given store-map." }
  save-map [store-map]
  (update-entity (find-entity { :session-id (session-utils/session-id) }) { :data (conjure-str-utils/form-str store-map) }))

(defn
#^{ :doc "Retrieves the value stored in the data store for the given session id or the id in the request-map." }
  retrieve 
  ([] (retrieve (session-utils/session-id)))
  ([session-id]
    (when-let [row-values (find-entity { :session-id session-id })]
      (when-let [data (:data (first row-values))]
        (read-string data)))))

(defn
#^{ :doc "Deletes the given key-name from the session store." }
  delete [key-name]
  (when-let [ stored-map (retrieve)]
    (save-map (dissoc stored-map key-name))))

(defn
#^{ :doc "Stores the given value in the session from the request-map." }
  save [key-name value]
  (if-let [stored-map (retrieve)]
    (save-map (assoc stored-map key-name value))
    (create-session key-name value)))

(def session-store 
  { :init init, 
    :drop drop-session,
    :delete delete,
    :save save,
    :retrieve retrieve })