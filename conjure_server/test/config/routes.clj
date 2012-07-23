;; This file is used to route requests to the appropriate service and action.

(ns config.routes
  (:require [clout.core :as clout]))

(def ^:dynamic routes 
  {
    :compiled 
      [ { :route (clout/route-compile "/:service/:action/:id")
          :request-map { :service 'service, :action 'action, :params { :id 'id } } }

        { :route (clout/route-compile "/:service/:action")
          :request-map { :service 'service, :action 'action } }

        { :route (clout/route-compile "/:service")
          :request-map { :service 'service, :action "index" } }

        { :route (clout/route-compile "/")
          :request-map { :service "home", :action "index" } } ]

    ; If you want to use your own route function, uncomment the key below and add your function to the vector. You 
    ; function should take no arguments, and return a partial request map containing at least the service and action

    ;:functions []
     })

