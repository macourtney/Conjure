(ns test.generators.test-scaffold-generator
  (:use clojure.contrib.test-is
        generators.scaffold-generator))

(deftest test-field-pairs
  (is (= [["name" "string"]] (field-pairs ["name:string"])))
  (is (= [["name" "string"] ["count" "integer"]] (field-pairs ["name:string" "count:integer"])))
  (is (= [["name" "string"] ["count" "integer"] ["foo" "bar"]] (field-pairs ["name:string" "count:integer" "foo:bar"])))
  (is (= [] (field-pairs [])))
  (is (= [] (field-pairs nil)))
  (is (= [["name"]] (field-pairs ["name"]))))
  
(deftest test-field-column-spec
  (is (= "(string \"name\")" (field-column-spec ["name" "string"])))
  (is (= "(integer \"count\")" (field-column-spec ["count" "integer"])))
  (is (= "(text \"description\")" (field-column-spec ["description" "text"])))
  (is (= "(belongs-to \"puppy\")" (field-column-spec ["puppy-id" "belongs-to"])))
  (is (= "(date \"birth-day\")" (field-column-spec ["birth-day" "date"])))
  (is (= "(time-type \"birth-time\")" (field-column-spec ["birth-time" "time"])))
  (is (= "(date-time \"birth-day-and-time\")" (field-column-spec ["birth-day-and-time" "date-time"])))
  (is (= "(string \"name\")" (field-column-spec ["name"])))
  (is (thrown? RuntimeException (field-column-spec ["foo" "bar"])))
  (is (= "(string \"\")" (field-column-spec [""])))
  (is (= "" (field-column-spec [])))
  (is (= "" (field-column-spec nil))))
  
(deftest test-fields-spec-string
  (is (= "\n    (string \"name\")" (fields-spec-string ["name:string"])))
  (is (= 
    "\n    (string \"name\")\n    (integer \"count\")" 
    (fields-spec-string ["name:string" "count:integer"])))
  (is (= 
    "\n    (string \"name\")\n    (integer \"count\")\n    (text \"description\")" 
    (fields-spec-string ["name:string" "count:integer" "description:text"])))
  (is (= "" (fields-spec-string [])))
  (is (= "" (fields-spec-string nil))))

(deftest test-create-migration-up-content
  (is (= "(create-table \"dogs\" 
    (id))" (create-migration-up-content "dog" [])))
  (is (= "(create-table \"dogs\" 
    (id)
    (string \"name\"))" 
    (create-migration-up-content "dog" ["name:string"])))
  (is (= "(create-table \"dogs\" 
    (id)
    (string \"name\")
    (integer \"count\"))" 
    (create-migration-up-content "dog" ["name:string" "count:integer"])))
  (is (= "(create-table \"dogs\" 
    (id)
    (string \"name\")
    (integer \"count\")
    (text \"description\"))" 
    (create-migration-up-content "dog" ["name:string" "count:integer" "description:text"]))))

(deftest test-create-list-records-action
  (let [view-map { :params "model-name table-metadata records", 
                   :content "(list-records/render-view request-map model-name table-metadata records)"
                   :requires "[views.templates.list-records :as list-records]" }]
    (is (= { 
      :controller "(defn list-records [request-map]
  (render-view request-map \"dog\" (dog/table-metadata) (dog/find-records [true])))", 
      :view view-map } 
      (create-list-records-action "dog")))
    (is (= { 
      :controller "(defn list-records [request-map]
  (render-view request-map \"\" (/table-metadata) (/find-records [true])))", 
      :view view-map } 
      (create-list-records-action nil)))))

(deftest test-create-show-action
  (let [view-map { :params "model-name table-metadata record", 
                   :content "(show/render-view request-map model-name table-metadata record)"
                   :requires "[views.templates.show :as show]" }]
    (is (= { 
      :controller "(defn show [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map \"dog\" (dog/table-metadata) (dog/get-record (or id 1)))))", 
      :view view-map } 
      (create-show-action "dog")))
    (is (= { 
      :controller "(defn show [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map \"\" (/table-metadata) (/get-record (or id 1)))))", 
      :view view-map } 
      (create-show-action nil)))))

(deftest test-create-add-action
  (let [view-map { :params "model-name table-metadata", 
                   :content "(add/render-view request-map model-name table-metadata)"
                   :requires "[views.templates.add :as add]" }]
    (is (= { 
      :controller "(defn add [request-map]
  (render-view request-map \"dog\" (dog/table-metadata)))", 
      :view view-map } 
      (create-add-action "dog")))
    (is (= { 
      :controller "(defn add [request-map]
  (render-view request-map \"\" (/table-metadata)))", 
      :view view-map } 
      (create-add-action nil)))))

(deftest test-create-create-action
  (is (= { 
    :controller "(defn create [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (dog/insert record))
    (redirect-to request-map { :action \"list-records\" })))", 
    :view nil } 
    (create-create-action "dog")))
  (is (= { 
    :controller "(defn create [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (/insert record))
    (redirect-to request-map { :action \"list-records\" })))", 
    :view nil } 
    (create-create-action nil))))

(deftest test-create-edit-action
  (let [view-map { :params "table-metadata record", 
                   :content "(edit/render-view request-map table-metadata record)"
                   :requires "[views.templates.edit :as edit]" }]
    (is (= { :controller "(defn edit [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (dog/table-metadata) (dog/get-record (or id 1)))))", 
      :view view-map } 
      (create-edit-action "dog")))
    (is (= { :controller "(defn edit [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (/table-metadata) (/get-record (or id 1)))))", 
      :view view-map } 
      (create-edit-action nil)))))

(deftest test-create-save-action
  (is (= { 
    :controller "(defn save [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (dog/update record))
    (redirect-to request-map { :action \"show\", :id (or record 1) })))", 
    :view nil } 
    (create-save-action "dog")))
  (is (= { 
    :controller "(defn save [request-map]
  (let [record (:record (:params request-map))]
    (if record
      (/update record))
    (redirect-to request-map { :action \"show\", :id (or record 1) })))", 
    :view nil } 
    (create-save-action nil))))

(deftest test-create-delete-warning-action
  (let [view-map { :params "table-metadata record", 
                   :content "(delete-warning/render-view request-map table-metadata record)"
                   :requires "[views.templates.delete-warning :as delete-warning]" }]
    (is (= { :controller "(defn delete-warning [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (dog/table-metadata) (dog/get-record (or id 1)))))", 
      :view view-map } 
      (create-delete-warning-action "dog")))
    (is (= { :controller "(defn delete-warning [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (/table-metadata) (/get-record (or id 1)))))", 
      :view view-map } 
      (create-delete-warning-action nil)))))

(deftest test-create-delete-action
  (is (= { :controller "(defn delete [request-map]
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (dog/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list-records\" }))))", 
    :view nil } 
    (create-delete-action "dog")))
  (is (= { :controller "(defn delete [request-map]
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list-records\" }))))", 
    :view nil } 
    (create-delete-action nil))))

(deftest test-create-action-map
  (let [action-map (create-action-map "dog")]
    (is (:list-records action-map))
    (is (:show action-map))
    (is (:add action-map))
    (is (:create action-map))
    (is (:edit action-map))
    (is (:save action-map))
    (is (:delete action-map))))

(deftest test-create-controller-content
  (let [model "dog"]
    (is (create-controller-content model (create-action-map model)))))

(deftest test-create-view-content
  (let [model "dog"]
    (is (create-view-content model :list-records (create-action-map model)))
    (is (create-view-content model :show))))

(deftest test-extra-model-content
  (is (extra-model-content)))