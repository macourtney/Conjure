(ns controllers.template-controller
  (:use [conjure.controller.base])
  (:require [conjure.model.util :as model-util]
            [clojure.contrib.ns-utils :as ns-utils]
            [conjure.server.request :as request]))

(defn
#^{ :doc "Returns the model name from the given request-map" }
  model-name []
  (request/controller))

(defn
#^{ :doc "Returns the function with the given name in the model pointed to by the request-map." }
  model-fn [fn-name]
  (ns-resolve (ns-utils/get-ns (symbol (model-util/model-namespace (model-name)))) (symbol fn-name)))

(defn
#^{ :doc "Destroys the given record in the model determined by the request-map." }
  destroy-record [record]
  ((model-fn "destroy-record") record))

(defn
#^{ :doc "Updates the given record in the model determined by the request-map." }
  update [record]
  ((model-fn "update") record))

(defn
#^{ :doc "Inserts the given record in the model determined by the request-map." }
  insert [record]
  ((model-fn "insert") record))

(defaction delete
  (let [delete-id (request/id)]
    (do
      (if delete-id (destroy-record { :id delete-id }))
      (redirect-to { :action "list-records" }))))

(defaction ajax-edit
  (bind-by-controller-action :templates :ajax-edit [(model-name)]))

(defaction ajax-row
  (bind-by-controller-action :templates :ajax-row [(model-name)]))

(defaction delete-warning
  (bind-by-controller-action :templates :delete-warning [(model-name)]))

(defaction list-records
  (bind-by-controller-action :templates :list-records [(model-name)]))

(defaction ajax-delete
  (let [delete-id (request/id)]
    (do
      (if delete-id (destroy-record { :id delete-id }))
      (request/with-controller-action "templates" "empty"
        (bind-by-controller-action :templates :ajax-direct [])))))

(defaction ajax-save
  (let [record (request/record)]
    (if record
      (do
        (update record)
        (bind-by-controller-action :templates :ajax-save [(model-name) record])))))

(defaction ajax-show
  (bind-by-controller-action :templates :ajax-show [(model-name)]))

(defaction edit
  (let [id (request/id)]
    (if id
      (bind-by-controller-action :templates :edit [(model-name)])
      (redirect-to { :action "list-records", :params {} }))))

(defaction index
  (redirect-to { :action "list-records" }))

(defaction add
  (bind-by-controller-action :templates :add [(model-name)]))

(defaction ajax-add
  (let [record (request/record)]
    (if record
      (do
        (insert record)
        (let [created-record ((model-fn "find-record") record)]
          (bind-by-controller-action :templates :ajax-add [(model-name) created-record]))))))

(defaction create
  (let [record (request/record)]
    (if record
      (insert record))
    (redirect-to (select-keys request-map [:controller] ) { :action "list-records" })))

(defaction save
  (let [record (request/record)]
    (if record
      (update record))
    (redirect-to request-map { :action "show", :id (or record 1) })))

(defaction show
  (let [id (request/id)]
    (if id
      (bind-by-controller-action :templates :show [(model-name)])
      (redirect-to { :action "list-records", :params {} }))))