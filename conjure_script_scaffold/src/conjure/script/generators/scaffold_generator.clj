(ns conjure.script.generators.scaffold-generator
  (:require [clojure.string :as str-utils]
            [conjure.model.util :as model-util]
            [conjure.model.builder :as model-builder]
            [conjure.test.util :as test-util]
            [conjure.view.util :as view-util]
            [conjure.script.generators.flow-generator :as flow-generator]
            [conjure.script.generators.view-generator :as view-generator]
            [conjure.script.generators.model-generator :as model-generator]
            [conjure.script.generators.model-test-generator :as model-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate scaffold command." }
  scaffold-usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj scaffold <model> [field:type]*"))

(defn
#^{ :doc "Returns a lazy sequence of field name to field type pairs based on the given fields. Fields is expected to be 
a sequence of strings of the form \"field:type\"" }
  field-pairs [fields]
  (map #(str-utils/split % #":") fields))
  
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
#^{ :doc "Returns the content for the scaffold flow." }
  create-flow-content [flow-name]
  (flow-generator/generate-flow-content flow-name "(copy-actions :template)" 
    "[flows.template-flow :as template-flow]"))

(defn
#^{ :doc "Creates the flow file associated with the given model." }
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
            (model-generator/model-file-content model))
          (model-test-generator/generate-unit-test model)
            (flow-generator/create-flow-files 
              { :service model, :flow-content (create-flow-content model) }))
        (scaffold-usage))))
        
(defn 
#^{ :doc "Generates a scaffold from the given parameters." }
  generate [params]
  (generate-scaffold (first params) (rest params)))