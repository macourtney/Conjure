(ns bindings.templates.direct
  (:use conjure.core.binding.base))

(def-binding [& params]
  (apply render-view params))