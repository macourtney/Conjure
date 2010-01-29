(ns views.templates.record-cell
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview [record record-key]
  (html/htmli
    (if (= :id record-key)
      [:td (link-to (helpers/h (get record record-key)) request-map { :action "show", :id (:id record) })]
      (let [record-key-str (conjure-str-utils/str-keyword record-key)]
        (if (. record-key-str endsWith "_id")
          (let [belongs-to-model (conjure-str-utils/strip-ending record-key-str "_id")
                belongs-to-id (helpers/h (get record record-key))]
            [:td (link-to belongs-to-id request-map { :controller belongs-to-model, :action "show", :id belongs-to-id })])
          [:td (helpers/h (get record record-key))])))))