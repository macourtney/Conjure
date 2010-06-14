(ns bindings.templates.direct
  (:use conjure.binding.base))

(def-binding [& params]
  (apply render-view params))