(ns conjure.model.session-store
  (:require [environment :as environment]))

(defn 
#^{ :doc "Saves the given value with the given key-name into the session store using the session from request-map." }
  save [request-map key-name value]
  ((:save environment/session-store) request-map key-name value))

(defn
#^{ :doc "Retrieves the entire session map from the session store using the session from request-map." }
  retrieve [request-map]
  ((:retrieve environment/session-store) request-map))

(defn
#^{ :doc "Retrieves the value for the given key-name from the session store using the session from request-map." }
  retrieve-value [request-map key-name]
  (get (retrieve request-map) key-name))

(defn
#^{ :doc "Deletes the value for the given key-name from the session store using the session from request-map." }
  delete [request-map key-name]
  ((:delete environment/session-store) request-map key-name))

(defn
#^{ :doc "Drops the entire session map from the session store using the session from request-map." }
  drop-session [request-map]
  ((:drop environment/session-store) request-map))
  
