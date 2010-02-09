(ns views.templates.ajax-show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-view :as record-view]))

(defview [model-name table-metadata record column-count]
  (let [row-id (str "row-" (:id record))]
    (html/html
      [:tr { :id row-id }
        [:td { :colspan column-count }
          [:div { :id (str "show-div-" (:id record)) }
            [:h3 (or (:name record) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
            (record-view/render-view request-map table-metadata record)
            (link-to-remote "Edit" request-map
              { :update (success-fn row-id :replace)
                :action "ajax-edit"
                :id record })
            "&nbsp;"
            (link-to-remote "Hide" request-map
              { :update (success-fn row-id :replace)
                :action "ajax-row"
                :id record })]]])))