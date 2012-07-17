;; This file is used to route requests to the appropriate controller and action.

(ns config.routes
  (:require [clout.core :as clout]))

(def routes 
  {
    :compiled 
      [ { :route (clout/route-compile "/:controller/:action/:id")
          :request-map { :controller 'controller, :action 'action, :params { :id 'id } } }

        { :route (clout/route-compile "/:controller/:action")
          :request-map { :controller 'controller, :action 'action } }

        { :route (clout/route-compile "/:controller")
          :request-map { :controller 'controller, :action "index" } }

        { :route (clout/route-compile "/")
          :request-map { :controller "home", :action "index" } } ]

    ; If you want to use your own route function, uncomment the key below and add your function to the vector. You 
    ; function should take no arguments, and return a partial request map containing at least the controller and action

    ; :functions []
  })

