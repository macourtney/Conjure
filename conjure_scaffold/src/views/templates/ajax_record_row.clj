(ns views.templates.ajax-record-row
  (:use conjure.view.base)
  (:require [conjure.view.util :as view-utils]
            [views.templates.record-row :as record-row]))

(def-ajax-view [record]
  (record-row/render-body record))