(ns views.templates.ajax-show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-view :as record-view]))

(defview [model-name table-metadata record column-count]
  (let [row-id (str "row-" (:id record))]
    [:tr { :id row-id }
      [:td { :colspan column-count }
        [:div { :id (str "show-div-" (:id record)) }
          [:h3 (or (helpers/h (:name record)) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
          (record-view/render-body table-metadata record)
          (ajax-link-to "Edit"
            { :update (success-fn row-id :replace)
              :action "ajax-edit"
              :controller model-name
              :params { :id record } })
          "&nbsp;"
          (ajax-link-to "Hide"
            { :update (success-fn row-id :replace)
              :action "ajax-row"
              :controller model-name
              :params { :id record } })]]]))