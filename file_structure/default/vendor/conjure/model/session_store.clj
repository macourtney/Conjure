(ns conjure.model.session-store
  (:require session-config))

(defn 
#^{ :doc "Saves the given value with the given key-name into the session store using the session from request-map." }
  save [request-map key-name value]
  ((:save session-config/session-store) request-map key-name value))

(defn
#^{ :doc "Retrieves the entire session map from the session store using the session from request-map." }
  retrieve [request-map]
  ((:retrieve session-config/session-store) request-map))

(defn
#^{ :doc "Retrieves the value for the given key-name from the session store using the session from request-map." }
  retrieve-value [request-map key-name]
  (get (retrieve request-map) key-name))

(defn
#^{ :doc "Deletes the value for the given key-name from the session store using the session from request-map." }
  delete [request-map key-name]
  ((:delete session-config/session-store) request-map key-name))

(defn
#^{ :doc "Drops the entire session map from the session store using the session from request-map." }
  drop-session [request-map]
  ((:drop session-config/session-store) request-map))
  
