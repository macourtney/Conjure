(ns config.session-config
  (:require [conjure.core.model.database-session-store :as database-session-store]))

(def use-session-cookie true) ; Causes Conjure to save session ids as cookies. If this is false, Conjure uses a parameter in html.

(def session-store database-session-store/session-store)