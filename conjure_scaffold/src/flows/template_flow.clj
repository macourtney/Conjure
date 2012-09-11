(ns flows.template-flow
  (:use [conjure.flow.base])
  (:require [conjure.model.util :as model-util]
            [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.add :as add-view]
            [views.templates.ajax-edit :as ajax-edit-view]
            [views.templates.ajax-record-row :as ajax-record-row-view]
            [views.templates.ajax-show :as ajax-show-view]
            [views.templates.delete-warning :as delete-warning-view]
            [views.templates.edit :as edit-view]
            [views.templates.empty :as empty-view]
            [views.templates.list-records :as list-records-view]
            [views.templates.show :as show-view]))

(defn
#^{ :doc "Returns the model name from the given request-map" }
  model-name []
  (request/service))

(defn
#^{ :doc "Returns the function with the given name in the model pointed to by the request-map." }
  model-fn [fn-name]
  (ns-resolve (find-ns (symbol (model-util/model-namespace (model-name)))) (symbol fn-name)))

(defn
#^{ :doc "Destroys the given record in the model determined by the request-map." }
  destroy-record [record]
  ((model-fn "destroy-record") record))

(defn
#^{ :doc "Inserts the given record in the model determined by the request-map." }
  insert [record]
  ((model-fn "insert") record))

(defn
#^{ :doc "Updates the given record in the model determined by the request-map." }
  update [record]
  ((model-fn "update") record))

(def-action add
  (add-view/render-view))

(def-action create
  (when-let [record (request/record)]
    (insert record))
  (redirect-to { :action "list-records", :params {} }))

(def-action delete
  (when-let [delete-id (request/id)]
    (destroy-record { :id delete-id }))
  (redirect-to { :action "list-records", :params {} }))

(def-action delete-warning
  (delete-warning-view/render-view))

(def-action edit
  (when-let [id (request/id)]
    (edit-view/render-view))
  (redirect-to { :action "list-records", :params {} }))

(def-action index
  (redirect-to { :action "list-records" }))

(def-action list-records
  (list-records-view/render-view))

(def-action save
  (if-let [record (request/record)]
    (do
      (update record)
      (redirect-to { :action "show", :id record }))
    (redirect-to { :action "show", :id 1 })))

(def-action show
  (if-let [id (request/id)]
    (show-view/render-view)
    (redirect-to { :action "list-records", :params {} })))

(def-action ajax-add
  (when-let [record (request/record)]
    (insert record)
    (ajax-record-row-view/render-view ((model-fn "find-record") record))))

(def-action ajax-edit
  (when-let [id (request/id)]
    (ajax-edit-view/render-view (template-helper/get-record (request/service) id))))

(def-action ajax-delete
  (when-let [delete-id (request/id)]
    (destroy-record { :id delete-id }))
  (empty-view/render-view))

(def-action ajax-row
  (when-let [id (request/id)]
    (ajax-record-row-view/render-view (template-helper/get-record (request/service) id))))

(def-action ajax-save
  (when-let [record (request/record)]
    (update record)
    (ajax-record-row-view/render-view record)))

(def-action ajax-show
  (when-let [id (request/id)]
    (ajax-show-view/render-view (template-helper/get-record (request/service) id))))