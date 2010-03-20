(ns views.templates.ajax-edit
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-form :as record-form]))

(defview [model-name table-metadata record column-count]
  (let [row-id (str "row-" (:id record))]
    (html/html
      [:tr { :id row-id }
        [:td { :colspan column-count }
          [:div { :id (str "show-div-" (:id record)) }
            [:h3 (or (helpers/h (:name record)) (str "Editing a " (conjure-str-utils/human-title-case model-name)))]
            (ajax-form-for request-map 
                { :name "ajax-save", 
                  :action "ajax-save", 
                  :update (success-fn row-id :replace) }
              (list
                (hidden-field record :record :id)
                (record-form/render-view request-map table-metadata record)
                (form-button "Save")
                "&nbsp;"
                (ajax-link-to "Hide" request-map
                  { :update (success-fn row-id :replace)
                    :action "ajax-row"
                    :params { :id record } })))]]])))