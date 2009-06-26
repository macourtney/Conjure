(ns conjure.controller.base
  (:require [conjure.view.util :as view-util]))

(defn
#^{:doc "Renders the view given in the request-map."}
  render-view [request-map & params]
  (view-util/load-view request-map)
  (apply
    (eval (read-string (str (view-util/request-view-namespace request-map) "/render-view")))
    request-map params))