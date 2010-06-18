(ns helpers.home-helper
  (:require [conjure.core.server.request :as request]))

(defn
#^{ :doc "Creates the home links and adds them to the layout info in request-map." }
  home-request-map [request-map]
  (assoc 
      request-map 
      :layout-info 
      { :links 
        [{ :text "Home", :url-for { :controller :home, :action :index } }]}))

(defmacro
  with-home-request-map [& body]
  `(request/with-request-map-fn home-request-map ~@body))