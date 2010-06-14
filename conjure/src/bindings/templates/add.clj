(ns bindings.templates.add
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]
            [views.templates.add :as add]))

(def-binding [model-name]
  (add/render-view model-name (template-helper/table-metadata model-name)))