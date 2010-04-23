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

(deftest test-create-controller-content
  (is (create-controller-content "dog")))