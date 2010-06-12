;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as contrib-str-utils]
            [conjure.controller.util :as controller-util]
            [conjure.server.request :as request]
            [conjure.util.loading-utils :as loading-utils]))

(defn
#^{ :doc "Given a path, this function returns the controller, action and id in a map." }
  parse-path [path]
  (when path
    (let [path-groups (re-matches #"/?(([^/]+)/?(([^/]+)/?([^/]+)?)?)?" path)]
      (when path-groups
        (let [controller (or (nth path-groups 2 nil) "home")
              action (or (nth path-groups 4 nil) "index")
              id (nth path-groups 5 nil)
              path-map { :controller (loading-utils/underscores-to-dashes controller)
                         :action (loading-utils/underscores-to-dashes action) }]
          (if id
            (assoc path-map :id id)
            path-map))))))

(defn
#^{ :doc "Calls the controller action specified in the given path-map. Path-map must contain :controller and :action 
keys, and may contain optional :id key. The controller, action and id will be appropriately merged into the request-map
before calling the controller action." }
  call-controller [{ :keys [controller action id] }]
  (request/with-controller-action-id controller action id
    (controller-util/call-controller)))

(defn
#^{ :doc "This function calls the appropriate controller and action." }
  route-request []
  (let [path-map (parse-path (request/uri))]
    (when path-map
      (logging/debug "Using default router.")
      (call-controller path-map))))