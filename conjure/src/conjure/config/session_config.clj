(ns conjure.config.session-config
  (:require [conjure.model.database-session-store :as database-session-store]
            [conjure.util.loading-utils :as loading-utils]))

(def use-session-cookie true) ; Causes Conjure to save session ids as cookies. If this is false, Conjure uses a parameter in html.

(def default-session-store database-session-store/session-store)

(defn
#^{ :doc "Returns the value of the given var symbol in the session-config namespace or default if the var or the namespace
cannot be found.." }
  resolve-session-config-var [var-sym default]
  (loading-utils/resolve-ns-var 'config.session-config var-sym default))
      
(defn
  use-session-cookie? []
  (resolve-session-config-var 'use-session-cookie use-session-cookie))
  
(defn
  session-store []
  (resolve-session-config-var 'session-store session-store))