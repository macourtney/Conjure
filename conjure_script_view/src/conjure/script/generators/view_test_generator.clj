(ns conjure.script.generators.view-test-generator
  (:require [clojure.tools.file-utils :as file-utils]
            [conjure.script.view.util :as script-view-util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.view.util :as util]))

(defn generate-test-content
  [service action]
  (let [test-namespace (test-util/view-unit-test-namespace service action)
        view-namespace (util/view-namespace-by-action service action)]
    (str "(ns " test-namespace "
  (:use clojure.test
        " view-namespace ")
  (:require [conjure.core.server.request :as request]))

(def service-name \"" service "\")
(def view-name \"" action "\")
(def request-map { :service service-name
                   :action view-name } )

(deftest test-view
  (request/set-request-map request-map
    (is (render-view))))")))

(defn
#^{:doc "Generates the view unit test file for the given service and action."}
  generate-unit-test 
  ([service action] (generate-unit-test service action false))
  ([service action silent] (generate-unit-test service action silent nil))
  ([service action silent incoming-content]
  (when-let [unit-test-file (test-builder/create-view-unit-test service action silent)]
    (file-utils/write-file-content unit-test-file (or incoming-content (generate-test-content service action))))))

(defn 
#^{:doc "Generates a service file for the service name and actions in params."}
  generate [params]
  (generate-unit-test (first params) (second params)))