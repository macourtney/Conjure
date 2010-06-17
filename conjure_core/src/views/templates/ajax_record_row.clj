(ns views.templates.ajax-record-row
  (:use conjure.core.view.base)
  (:require [conjure.core.view.util :as view-utils]
            [views.templates.record-row :as record-row]))

(def-ajax-view [model-name table-metadata record]
  (record-row/render-body model-name table-metadata record))