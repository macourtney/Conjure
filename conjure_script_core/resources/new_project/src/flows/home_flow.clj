(ns flows.home-flow
  (:use conjure.flow.base
        helpers.home-helper)
  (:require [conjure.view.util :as view-util]))

(def-action index
  (view-util/render-view))

(def-action list-records
  (redirect-to { :action "index" }))

(def-action add
  (redirect-to { :action "index" }))

(def-action error-404
  (view-util/render-view))