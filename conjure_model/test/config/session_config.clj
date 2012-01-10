(ns config.session-config
  (:require [conjure.core.model.memory-session-store :as memory-session-store]))

(def ^:dynamic use-session-cookie true) ; Causes Conjure to save session ids as cookies. If this is false, Conjure uses a parameter in html.

(def session-store memory-session-store/session-store)