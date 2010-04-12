(ns generators.scaffold-generator
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.model.util :as model-util]
            [conjure.model.builder :as model-builder]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.test.util :as test-util]
            [conjure.view.util :as view-util]
            [generators.controller-generator :as controller-generator]
            [generators.view-generator :as view-generator]
            [generators.model-generator :as model-generator]
            [generators.model-test-generator :as model-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate controller command." }
  scaffold-usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj scaffold <model> [field:type]*"))

(defn
#^{ :doc "Returns a lazy sequence of field name to field type pairs based on the given fields. Fields is expected to be 
a sequence of strings of the form \"field:type\"" }
  field-pairs [fields]
  (map #(str-utils/re-split #":" %) fields))
  
(defn
#^{ :doc "Returns a string for the column spec for the given field pair." }
  field-column-spec [field-pair]
  (let [field-name (first field-pair)
        field-type (second field-pair)]
    (if field-name
      (if field-type
        (cond 
          (= "integer" field-type) (str "(integer \"" field-name "\")")
          (= "string" field-type) (str "(string \"" field-name "\")")
          (= "text" field-type) (str "(text \"" field-name "\")")
          (= "date" field-type) (str "(date \"" field-name "\")")
          (= "time" field-type) (str "(time-type \"" field-name "\")")
          (= "date-time" field-type) (str "(date-time \"" field-name "\")")
          (= "belongs-to" field-type) (str "(belongs-to \"" (model-util/to-model-name field-name) "\")")
          true (throw (new RuntimeException (str "Unknown field type: " field-type ", for field:" field-name))))
        (str "(string \"" field-name "\")"))
      "")))

(defn
#^{ :doc "Returns a string of specs from the given fields. 

For example: if fields is [\"name:string\" \"count:integer\"] this method would return 
\"    (string name)\n    (integer count)\"" }
  fields-spec-string [fields]
  (apply str (interleave (repeat "\n    ") (map field-column-spec (field-pairs fields)))))

(defn
#^{ :doc "Returns the content for the up function of the create migration for the given model." }
  create-migration-up-content [model fields]
  (str "(create-table \"" (model-util/model-to-table-name model) "\" 
    (id)"
    (fields-spec-string fields)
    ")"))

(defn
#^{ :doc "Returns the content for add in the action map." }
  create-index-action []
    { :controller (str "(defaction index
  (redirect-to request-map { :action \"list-records\" }))")
      :view nil })
      
(defn
#^{ :doc "Returns the content for a view test." }
  create-view-test [model action params include-fixture?]
  (str "(ns " (test-util/view-unit-test-namespace model action) "
  (:use clojure.contrib.test-is
        " (view-util/view-namespace-by-action model action) ")
  (:require [" (model-util/model-namespace model) " :as model]"
  (if include-fixture? (str "\n            [" (test-util/fixture-namespace model) " :as fixture]"))
"))

(def controller-name \"" model "\")
(def view-name \""action"\")
(def request-map { :controller controller-name
                   :action view-name } )

(deftest test-view
  (render-view request-map " params "))"))

(defn
#^{ :doc "Returns the content for a view test using a single record, the model
name and the table metadata as params." }
  create-view-test-with-model-and-one-record [model action]
  (create-view-test model action 
    (str "\"" model "\" (model/table-metadata) (first fixture/records)") true))

(defn
#^{ :doc "Returns the content for a view test using a single record and the 
table metadata as params." }
  create-view-test-with-one-record [model action]
  (create-view-test model action 
    (str "(model/table-metadata) (first fixture/records)") true))

(defn
#^{ :doc "Returns the content for list in the action map." }
  create-list-records-action [model]
    { :controller (str "(defaction list-records
  (bind-by-controller-action :generic :list-records [request-map \"" model "\"]))")
      :view nil })
  
(defn
#^{ :doc "Returns the content for list in the action map." }
  create-show-action [model]
    { :controller (str "(defaction show
  (let [id (:id (:params request-map))]
    (if id
      (bind-by-controller-action :generic :show [request-map \"" model "\"])
      (redirect-to request-map { :action \"list-records\", :params {} }))))")
      :view nil })
  
(defn
#^{ :doc "Returns the content for add in the action map." }
  create-add-action [model]
    { :controller (str "(defaction add
  (bind-by-controller-action :generic :add [request-map \"" model "\"]))")
      :view nil })
      
(defn
#^{ :doc "Returns the content for add in the action map." }
  create-create-action [model]
    { :controller (str "(defaction create
  (let [record (:record (:params request-map))]
    (if record
      (" model "/insert record))
    (redirect-to (select-keys request-map [:controller] ) { :action \"list-records\" })))")
      :view nil })
  
(defn
#^{ :doc "Returns the content for edit in the action map." }
  create-edit-action [model]
    { :controller (str "(defaction edit
  (let [id (:id (:params request-map))]
    (if id
      (bind-by-controller-action :generic :edit [request-map \"" model "\"])
      (redirect-to request-map { :action \"list-records\", :params {} }))))")
      :view nil })

(defn
#^{ :doc "Returns the content for add in the action map." }
  create-save-action [model]
    { :controller (str "(defaction save
  (let [record (:record (:params request-map))]
    (if record
      (" model "/update record))
    (redirect-to request-map { :action \"show\", :id (or record 1) })))")
      :view nil })

(defn
#^{ :doc "Returns the content for delete in the action map." }
  create-delete-warning-action [model]
    { :controller (str "(defaction delete-warning
  (bind-by-controller-action :generic :delete-warning [request-map \"" model "\"]))")
      :view nil })

(defn
#^{ :doc "Returns the content for delete in the action map." }
  create-delete-action [model]
    { :controller (str "(defaction delete
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (" model "/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list-records\" }))))")
      :view nil })

(defn
#^{ :doc "Returns the content for the ajax delete in the action map." }
  create-ajax-delete [model]
  { :controller (str "(defaction ajax-delete
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (" model "/destroy-record { :id delete-id }))
      (bind-by-controller-action :generic :ajax-direct [(merge request-map { :controller \"templates\", :action \"empty\" })]))))")
      :view nil })

(defn
#^{ :doc "Returns the content for the ajax add in the action map." }
  create-ajax-add [model]
  { :controller (str "(defaction ajax-add
  (let [record (:record (:params request-map))]
    (if record
      (do
        (" model "/insert record)
        (let [created-record ("model"/find-record record)]
          (bind-by-controller-action :generic :ajax-add [request-map \"" model "\" created-record]))))))")
      :view nil })
    
(defn
#^{ :doc "Returns the content for the ajax show in the action map." }
  create-ajax-show [model]
  { :controller (str "(defaction ajax-show
  (bind-by-controller-action :generic :ajax-show [request-map \"" model "\"]))")
      :view nil })
          
(defn
#^{ :doc "Returns the content for the ajax row in the action map." }
  create-ajax-row [model]
  { :controller (str "(defaction ajax-row
  (bind-by-controller-action :generic :ajax-row [request-map \"" model "\"]))")
    :view nil })

(defn
#^{ :doc "Returns the content for the ajax show in the action map." }
  create-ajax-edit [model]
  { :controller (str "(defaction ajax-edit
  (bind-by-controller-action :generic :ajax-edit [request-map \"" model "\"]))")
    :view nil })
    
(defn
#^{ :doc "Returns the content for the ajax row in the action map." }
  create-ajax-save [model]
  { :controller (str "(defaction ajax-save
  (let [record (:record (:params request-map))]
    (if record
      (do
        (" model "/update record)
        (bind-by-controller-action :generic :ajax-save [request-map \"" model "\" record])))))")
    :view nil })
    
(defn
#^{ :doc "Returns a map which links action names to content and such." }
  create-action-map [model]
    { :index (create-index-action)
      :list-records (create-list-records-action model)
      :show (create-show-action model)
      :add (create-add-action model)
      :create (create-create-action model)
      :edit (create-edit-action model)
      :save (create-save-action model)
      :delete-warning (create-delete-warning-action model)
      :delete (create-delete-action model)
      :ajax-delete (create-ajax-delete model)
      :ajax-add (create-ajax-add model)
      :ajax-show (create-ajax-show model)
      :ajax-row (create-ajax-row model)
      :ajax-edit (create-ajax-edit model)
      :ajax-save (create-ajax-save model) })
    
(defn
#^{ :doc "Returns the content for the scaffold controller." }
  create-controller-content [controller-name action-map]
  (controller-generator/generate-controller-content 
    controller-name 
    (str-utils/str-join "\n\n" (map :controller (vals action-map)))
    (str "[models." controller-name " :as " controller-name "]")))
    
(defn
#^{ :doc "Returns the entire view content for the given action-map, action-name and controller-name" }
  create-view-content 
  ([controller-name action-name] (create-view-content controller-name action-name (create-action-map controller-name)))
  ([controller-name action-name action-map]
    (let [view-map (:view (get action-map action-name))]
      (if view-map
        (view-generator/generate-view-content 
          controller-name 
          action-name 
          (:content view-map) 
          (:params view-map) 
          (:requires view-map))))))
    
(defn
#^{ :doc "Creates all of the actions for a scaffold." }
  generate-views [controller-name action-map]
  (doall 
    (map 
      (fn [action-name] 
        (if (get (get action-map action-name) :view)
          (view-generator/generate-view-file 
            { :controller controller-name, 
              :action action-name, 
              :content (create-view-content controller-name action-name action-map)
              :test-content (get (get action-map action-name) :view-test) })))
      (keys action-map))))

(defn
#^{ :doc "Creates the extra model functions for generated models." }
  extra-model-content []
  "(defn
#^{ :doc \"Returns the metadata for the table associated with this model.\" }
  table-metadata []
    (doall (find-by-sql [(str \"SHOW COLUMNS FROM \" (table-name))])))")

(defn
#^{ :doc "Creates the controller file associated with the given controller." }
  generate-scaffold
    ([model fields]
      (if (and model fields)
        (do
          (model-generator/generate-migration-file 
            model 
            (create-migration-up-content model fields) 
            (model-generator/create-migration-down-content model))
          (model-generator/create-model-file 
            model 
            (model-builder/create-model-file (model-util/find-models-directory) model)
            (model-generator/model-file-content model (extra-model-content)))
          (model-test-generator/generate-unit-test model)
          (let [action-map (create-action-map model)
                actions (map conjure-str-utils/str-keyword (keys action-map))]
            (controller-generator/create-controller-files 
              { :controller model, :controller-content (create-controller-content model action-map), :actions actions })
            (generate-views model action-map)))
        (scaffold-usage))))
        
(defn 
#^{ :doc "Generates a scaffold from the given parameters." }
  generate [params]
  (generate-scaffold (first params) (rest params)))