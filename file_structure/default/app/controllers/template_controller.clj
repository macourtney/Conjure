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
#^{ :doc "Inserts the given record in the model determined by the request-map." }
  insert [record]
  ((model-fn "insert") record))

(defn
#^{ :doc "Updates the given record in the model determined by the request-map." }
  update [record]
  ((model-fn "update") record))

(def-action add
  (bind-by-controller-action :templates :add [(model-name)]))

(def-action create
  (let [record (request/record)]
    (if record
      (insert record))
    (redirect-to { :action "list-records", :params {} })))

(def-action delete
  (let [delete-id (request/id)]
    (do
      (if delete-id (destroy-record { :id delete-id }))
      (redirect-to { :action "list-records", :params {} }))))

(def-action delete-warning
  (bind-by-controller-action :templates :delete-warning [(model-name)]))

(def-action edit
  (let [id (request/id)]
    (if id
      (bind-by-controller-action :templates :edit [(model-name)])
      (redirect-to { :action "list-records", :params {} }))))

(def-action index
  (redirect-to { :action "list-records" }))

(def-action list-records
  (bind-by-controller-action :templates :list-records [(model-name)]))

(def-action save
  (let [record (request/record)]
    (if record
      (update record))
    (redirect-to { :action "show", :id (or record 1) })))

(def-action show
  (let [id (request/id)]
    (if id
      (bind-by-controller-action :templates :show [(model-name)])
      (redirect-to { :action "list-records", :params {} }))))

(def-action ajax-add
  (let [record (request/record)]
    (if record
      (do
        (insert record)
        (let [created-record ((model-fn "find-record") record)]
          (bind-by-controller-action :templates :ajax-add [(model-name) created-record]))))))

(def-action ajax-edit
  (bind-by-controller-action :templates :ajax-edit [(model-name)]))

(def-action ajax-delete
  (let [delete-id (request/id)]
    (do
      (if delete-id (destroy-record { :id delete-id }))
      (request/with-controller-action "templates" "empty"
        (bind-by-controller-action :templates :direct [])))))

(def-action ajax-row
  (bind-by-controller-action :templates :ajax-row [(model-name)]))

(def-action ajax-save
  (let [record (request/record)]
    (if record
      (do
        (update record)
        (bind-by-controller-action :templates :ajax-save [(model-name) record])))))

(def-action ajax-show
  (bind-by-controller-action :templates :ajax-show [(model-name)]))