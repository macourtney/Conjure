(ns conjure.core.view.test-xml-base
  (:use clojure.test
        conjure.core.view.xml-base))

(def-xml []
  { :tag :test })

(deftest test-defxml
  (is (= { :status 200, :headers { "Content-Type" "text/xml" }, :body "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test/>" } (render-view))))