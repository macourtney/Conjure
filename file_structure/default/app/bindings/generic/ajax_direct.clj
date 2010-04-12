(ns bindings.generic.ajax-direct
  (:use conjure.binding.base))

(defbinding [request-map & params]
  (apply render-view { :layout nil } request-map params))