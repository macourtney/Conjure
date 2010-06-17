(ns conjure.core.model.session-store
  (:require [config.session-config :as session-config]))

(defn 
#^{ :doc "Saves the given value with the given key-name into the session store using the session from the
request-map." }
  save [key-name value]
  ((:save session-config/session-store) key-name value))

(defn
#^{ :doc "Retrieves the entire session map from the session store using the session from the request-map." }
  retrieve []
  ((:retrieve session-config/session-store)))

(defn
#^{ :doc "Retrieves the value for the given key-name from the session store using the session from the request-map." }
  retrieve-value [key-name]
  (get (retrieve) key-name))

(defn
#^{ :doc "Deletes the value for the given key-name from the session store using the session from the request-map." }
  delete [key-name]
  ((:delete session-config/session-store) key-name))

(defn
#^{ :doc "Drops the entire session map from the session store using the session from the request-map." }
  drop-session []
  ((:drop session-config/session-store)))
  
