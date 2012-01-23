(ns conjure.core.view.test-xml-base
  (:use clojure.test
        conjure.core.view.xml-base))

(def-xml []
  [:test])

(deftest test-defxml
  (is (= { :status 200, :headers { "Content-Type" "text/xml" }, :body ["<" "test" " />"] }
         (render-view))))