(ns generators.scaffold-generator
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.model.util :as model-util]
            [conjure.model.builder :as model-builder]
            [conjure.util.string-utils :as conjure-str-utils]
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
#^{ :doc "Returns the content for list in the action map." }
  create-list-records-action [model]
    { :controller (str "(defn list-records [request-map]
  (render-view request-map \"" model "\" (" model "/table-metadata) (" model "/find-records [true])))")
      :view 
        { :params "model-name table-metadata records", 
          :content "(list-records/render-view request-map model-name table-metadata records)" 
          :requires "[views.templates.list-records :as list-records]" } })
  
(defn
#^{ :doc "Returns the content for list in the action map." }
  create-show-action [model]
    { :controller (str "(defn show [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map \"" model "\" (" model "/table-metadata) (" model "/get-record (or id 1)))))")
      :view 
        { :params "model-name table-metadata record", 
          :content "(show/render-view request-map model-name table-metadata record)" 
          :requires "[views.templates.show :as show]" } })
  
(defn
#^{ :doc "Returns the content for add in the action map." }
  create-add-action [model]
    { :controller (str "(defn add [request-map]
  (render-view request-map \"" model "\" (" model "/table-metadata)))")
      :view 
        { :params "model-name table-metadata", 
          :content "(add/render-view request-map model-name table-metadata)"
          :requires "[views.templates.add :as add]" } })
      
(defn
#^{ :doc "Returns the content for add in the action map." }
  create-create-action [model]
    { :controller (str "(defn create [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (" model "/insert record))
    (redirect-to request-map { :action \"list-records\" })))")
      :view nil })
  
(defn
#^{ :doc "Returns the content for edit in the action map." }
  create-edit-action [model]
    { :controller (str "(defn edit [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (" model "/table-metadata) (" model "/get-record (or id 1)))))")
      :view 
        { :params "table-metadata record", 
          :content "(edit/render-view request-map table-metadata record)"
          :requires "[views.templates.edit :as edit]" } })

(defn
#^{ :doc "Returns the content for add in the action map." }
  create-save-action [model]
    { :controller (str "(defn save [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (" model "/update record))
    (redirect-to request-map { :action \"show\", :id (or record 1) })))")
      :view nil })

(defn
#^{ :doc "Returns the content for delete in the action map." }
  create-delete-warning-action [model]
    { :controller (str "(defn delete-warning [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (" model "/table-metadata) (" model "/get-record (or id 1)))))")
      :view { :params "table-metadata record", 
          :content "(delete-warning/render-view request-map table-metadata record)"
          :requires "[views.templates.delete-warning :as delete-warning]" } })

(defn
#^{ :doc "Returns the content for delete in the action map." }
  create-delete-action [model]
    { :controller (str "(defn delete [request-map]
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (" model "/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list-records\" }))))")
      :view nil })
    
(defn
#^{ :doc "Returns a map which links action names to content and such." }
  create-action-map [model]
    { :list-records (create-list-records-action model)
      :show (create-show-action model)
      :add (create-add-action model)
      :create (create-create-action model)
      :edit (create-edit-action model)
      :save (create-save-action model)
      :delete-warning (create-delete-warning-action model)
      :delete (create-delete-action model) })
    
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
            controller-name 
            action-name 
            (create-view-content controller-name action-name action-map))))
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
            (controller-generator/create-controller-files model (create-controller-content model action-map) actions)
            (generate-views model action-map)))
        (scaffold-usage))))
        
(defn 
#^{ :doc "Generates a scaffold from the given parameters." }
  generate [params]
  (generate-scaffold (first params) (rest params)))