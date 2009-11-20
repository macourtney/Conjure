(ns views.templates.record-form
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.utils :as utils]))

(defview [record]
  (html/html
    (utils/domap-str [record-key (keys record)]
      (html/html
        [:p record-key ": " (text-field record "record" record-key)]
        [:br]))))