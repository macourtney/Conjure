(ns views.templates.ajax-record-row
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-utils]
            [views.templates.record-row :as record-row]))

(def-ajax-view [model-name table-metadata record]
  (record-row/render-body model-name table-metadata record))