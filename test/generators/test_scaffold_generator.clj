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
  (is (= "(database/string \"name\")" (field-column-spec ["name" "string"])))
  (is (= "(database/integer \"count\")" (field-column-spec ["count" "integer"])))
  (is (= "(database/text \"description\")" (field-column-spec ["description" "text"])))
  (is (= "(database/belongs-to \"puppy\")" (field-column-spec ["puppy-id" "belongs-to"])))
  (is (= "(database/string \"name\")" (field-column-spec ["name"])))
  (is (thrown? RuntimeException (field-column-spec ["foo" "bar"])))
  (is (= "(database/string \"\")" (field-column-spec [""])))
  (is (= "" (field-column-spec [])))
  (is (= "" (field-column-spec nil))))
  
(deftest test-fields-spec-string
  (is (= "\n    (database/string \"name\")" (fields-spec-string ["name:string"])))
  (is (= 
    "\n    (database/string \"name\")\n    (database/integer \"count\")" 
    (fields-spec-string ["name:string" "count:integer"])))
  (is (= 
    "\n    (database/string \"name\")\n    (database/integer \"count\")\n    (database/text \"description\")" 
    (fields-spec-string ["name:string" "count:integer" "description:text"])))
  (is (= "" (fields-spec-string [])))
  (is (= "" (fields-spec-string nil))))

(deftest test-create-migration-up-content
  (is (= "(database/create-table \"dogs\" 
    (database/id))" (create-migration-up-content "dog" [])))
  (is (= "(database/create-table \"dogs\" 
    (database/id)
    (database/string \"name\"))" 
    (create-migration-up-content "dog" ["name:string"])))
  (is (= "(database/create-table \"dogs\" 
    (database/id)
    (database/string \"name\")
    (database/integer \"count\"))" 
    (create-migration-up-content "dog" ["name:string" "count:integer"])))
  (is (= "(database/create-table \"dogs\" 
    (database/id)
    (database/string \"name\")
    (database/integer \"count\")
    (database/text \"description\"))" 
    (create-migration-up-content "dog" ["name:string" "count:integer" "description:text"]))))

(deftest test-create-list-records-action
  (let [view-map { :params "records", 
                   :content "(list-records/render-view request-map records)"
                   :requires "[views.templates.list-records :as list-records]" }]
    (is (= { 
      :controller "(defn list-records [request-map]
  (render-view request-map (dog/find-records [true])))", 
      :view view-map } 
      (create-list-records-action "dog")))
    (is (= { 
      :controller "(defn list-records [request-map]
  (render-view request-map (/find-records [true])))", 
      :view view-map } 
      (create-list-records-action nil)))))

(deftest test-create-show-action
  (let [view-map { :params "record", 
                   :content "(show/render-view request-map record)"
                   :requires "[views.templates.show :as show]" }]
    (is (= { 
      :controller "(defn show [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (dog/get-record (or id 1)))))", 
      :view view-map } 
      (create-show-action "dog")))
    (is (= { 
      :controller "(defn show [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (/get-record (or id 1)))))", 
      :view view-map } 
      (create-show-action nil)))))

(deftest test-create-add-action
  (is (= { 
    :controller "(defn add [request-map]
  (render-view request-map))", 
    :view { :params "", :content "(add/render-view request-map)" :requires "[views.templates.add :as add]" } } 
    (create-add-action))))

(deftest test-create-create-action
  (is (= { 
    :controller "(defn create [request-map]
  (let [params (:params request-map)]
    (if params
      (dog/insert params))
    (redirect-to request-map { :action \"list\" })))", 
    :view nil } 
    (create-create-action "dog")))
  (is (= { 
    :controller "(defn create [request-map]
  (let [params (:params request-map)]
    (if params
      (/insert params))
    (redirect-to request-map { :action \"list\" })))", 
    :view nil } 
    (create-create-action nil))))

(deftest test-create-edit-action
  (let [view-map { :params "record", 
                   :content "(edit/render-view request-map record)"
                   :requires "[views.templates.edit :as edit]" }]
    (is (= { :controller "(defn edit [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (dog/get-record (or id 1)))))", 
      :view view-map } 
      (create-edit-action "dog")))
    (is (= { :controller "(defn edit [request-map]
  (let [id (:id (:params request-map))]
    (render-view request-map (/get-record (or id 1)))))", 
      :view view-map } 
      (create-edit-action nil)))))

(deftest test-create-delete-action
  (is (= { :controller "(defn delete [request-map]
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (dog/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list\" }))))", 
    :view nil } 
    (create-delete-action "dog")))
  (is (= { :controller "(defn delete [request-map]
  (let [delete-id (:id (:params request-map))]
    (do
      (if delete-id (/destroy-record { :id delete-id }))
      (redirect-to request-map { :action \"list\" }))))", 
    :view nil } 
    (create-delete-action nil))))

(deftest test-create-action-map
  (let [action-map (create-action-map "dog")]
    (is (:list-records action-map))
    (is (:show action-map))
    (is (:add action-map))
    (is (:create action-map))
    (is (:edit action-map))
    (is (:delete action-map))))

(deftest test-create-controller-content
  (let [model "dog"]
    (is (create-controller-content model (create-action-map model)))))

(deftest test-create-view-content
  (let [model "dog"]
    (is (create-view-content model :list-records (create-action-map model)))
    (is (create-view-content model :show))))