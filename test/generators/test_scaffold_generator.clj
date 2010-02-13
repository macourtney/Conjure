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
  (is (create-list-records-action "dog"))
  (is (create-list-records-action nil)))

(deftest test-create-show-action
    (is (create-show-action "dog"))
    (is (create-show-action nil)))

(deftest test-create-add-action
  (is (create-add-action "dog"))
  (is (create-add-action nil)))

(deftest test-create-create-action
  (is (create-create-action "dog")))
  (is (create-create-action nil))

(deftest test-create-edit-action
  (is (create-edit-action "dog"))
  (is (create-edit-action nil)))

(deftest test-create-save-action
  (is (create-save-action "dog")))
  (is (create-save-action nil))

(deftest test-create-delete-warning-action
    (is (create-delete-warning-action "dog"))
    (is (create-delete-warning-action nil)))

(deftest test-create-delete-action
  (is (create-delete-action "dog")))
  (is (create-delete-action nil))

(deftest test-create-action-map
  (let [action-map (create-action-map "dog")]
    (is (:index action-map))
    (is (:list-records action-map))
    (is (:show action-map))
    (is (:add action-map))
    (is (:create action-map))
    (is (:edit action-map))
    (is (:save action-map))
    (is (:delete-warning action-map))
    (is (:delete action-map))
    (is (:ajax-delete action-map))
    (is (:ajax-add action-map))
    (is (:ajax-show action-map))
    (is (:ajax-row action-map))
    (is (:ajax-edit action-map))
    (is (:ajax-save action-map))))

(deftest test-create-controller-content
  (let [model "dog"]
    (is (create-controller-content model (create-action-map model)))))

(deftest test-create-view-content
  (let [model "dog"]
    (is (create-view-content model :list-records (create-action-map model)))
    (is (create-view-content model :show))))

(deftest test-extra-model-content
  (is (extra-model-content)))