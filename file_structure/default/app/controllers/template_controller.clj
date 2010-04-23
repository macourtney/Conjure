(ns controllers.template-controller
  (:use [conjure.controller.base])
  (:require [conjure.model.util :as model-util]
            [clojure.contrib.ns-utils :as ns-utils]))

(defn
#^{ :doc "Returns the model name from the given request-map" }
  model-name [request-map]
  (:controller request-map))

(defn
#^{ :doc "Returns the function with the given name in the model pointed to by the request-map." }
  model-fn [request-map fn-name]
  (ns-resolve (ns-utils/get-ns (symbol (model-util/model-namespace (model-name request-map)))) (symbol fn-name)))

(defn
#^{ :doc "Destroys the given record in the model determined by the request-map." }
  destroy-record [request-map record]
  ((model-fn request-map "destroy-record") record))

(defn
#^{ :doc "Updates the given record in the model determined by the request-map." }
  update [request-map record]
  ((model-fn request-map "update") record))

(defn
#^{ :doc "Inserts the given record in the model determined by the request-map." }
  insert [request-map record]
  ((model-fn request-map "insert") record))

(defaction delete
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (destroy-record request-map { :id delete-id }))
      (redirect-to request-map { :action "list-records" }))))

(defaction ajax-edit
  (bind-by-controller-action :templates :ajax-edit [request-map (model-name request-map)]))

(defaction ajax-row
  (bind-by-controller-action :templates :ajax-row [request-map (model-name request-map)]))

(defaction delete-warning
  (bind-by-controller-action :templates :delete-warning [request-map (model-name request-map)]))

(defaction list-records
  (bind-by-controller-action :templates :list-records [request-map (model-name request-map)]))

(defaction ajax-delete
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (destroy-record request-map { :id delete-id }))
      (bind-by-controller-action :templates :ajax-direct [(merge request-map { :controller "templates", :action "empty" })]))))

(defaction ajax-save
  (let [record (:record (:params request-map))]
    (if record
      (do
        (update request-map record)
        (bind-by-controller-action :templates :ajax-save [request-map (model-name request-map) record])))))

(defaction ajax-show
  (bind-by-controller-action :templates :ajax-show [request-map (model-name request-map)]))

(defaction edit
  (let [id (:id (:params request-map))]
    (if id
      (bind-by-controller-action :templates :edit [request-map (model-name request-map)])
      (redirect-to request-map { :action "list-records", :params {} }))))

(defaction index
  (redirect-to request-map { :action "list-records" }))

(defaction add
  (bind-by-controller-action :templates :add [request-map (model-name request-map)]))

(defaction ajax-add
  (let [record (:record (:params request-map))]
    (if record
      (do
        (insert request-map record)
        (let [created-record ((model-fn request-map "find-record") record)]
          (bind-by-controller-action :templates :ajax-add [request-map (model-name request-map) created-record]))))))

(defaction create
  (let [record (:record (:params request-map))]
    (if record
      (insert request-map record))
    (redirect-to (select-keys request-map [:controller] ) { :action "list-records" })))

(defaction save
  (let [record (:record (:params request-map))]
    (if record
      (update request-map record))
    (redirect-to request-map { :action "show", :id (or record 1) })))

(defaction show
  (let [id (:id (:params request-map))]
    (if id
      (bind-by-controller-action :templates :show [request-map (model-name request-map)])
      (redirect-to request-map { :action "list-records", :params {} }))))