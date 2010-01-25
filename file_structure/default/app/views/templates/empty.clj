(ns views.templates.empty
  (:use conjure.view.base)
  (:require [clj-html.core :as html]))

(defview []
  (html/html))