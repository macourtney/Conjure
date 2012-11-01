(ns flows.test-flow
  (:use conjure.flow.base)
  (:require [conjure.view.util :as view-util]))

(def-render-only-actions :list)

(def-action show
  (render))