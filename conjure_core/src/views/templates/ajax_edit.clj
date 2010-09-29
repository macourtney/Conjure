(ns views.templates.ajax-edit
  (:use conjure.core.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [hiccup.core :as hiccup]
            [views.templates.record-form :as record-form]))

(def-ajax-view [model-name table-metadata record column-count]
  (let [row-id (str "row-" (:id record))]
    [:tr { :id row-id }
      [:td { :colspan column-count }
        [:div { :id (str "show-div-" (:id record)) }
          [:h3 (or (hiccup/h (:name record)) (str "Editing a " (conjure-str-utils/human-title-case model-name)))]
          (ajax-form-for
              { :name "ajax-save", 
                :action "ajax-save",
                :controller model-name,
                :update (success-fn row-id :replace) }
            (list
              (hidden-field record :record :id)
              (record-form/render-body table-metadata record)
              (form-button "Save")
              "&nbsp;"
              (ajax-link-to "Hide"
                { :update (success-fn row-id :replace)
                  :action "ajax-row"
                  :controller model-name
                  :params { :id record } })))]]]))