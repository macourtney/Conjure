(ns test.view.test-xml-base
  (:use clojure.contrib.test-is
        conjure.view.xml-base))

(defxml [message]
  message)

(deftest test-defxml
  (is (= { :status 200, :headers { "Content-Type" "text/xml" }, :body "test" } (render-view {} "test"))))