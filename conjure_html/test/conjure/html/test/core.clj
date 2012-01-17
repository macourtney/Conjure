(ns conjure.html.test.core
  (:use [conjure.html.core])
  (:use [clojure.test]))

(deftest test-as-str ;; FIXME: write
  (is (= "" (as-str)))
  (is (= "" (as-str nil)))
  (is (= "blah" (as-str "blah")))
  (is (= "blah" (as-str :blah)))
  (is (= "1" (as-str 1)))
  (is (= "1.0" (as-str 1.0)))
  (is (= "blah blah 1" (as-str :blah " blah " 1))))

(deftest test-escape-xml
  (is (= "&amp;" (escape-xml "&")))
  (is (= "&lt;" (escape-xml "<")))
  (is (= "&gt;" (escape-xml ">")))
  (is (= "&quot;" (escape-xml "\"")))
  (is (= "&quot;You &amp; I&quot;" (escape-xml "\"You & I\"")))
  (is (= "" (escape-xml "")))
  (is (= "" (escape-xml nil))))

(deftest test-render-obj
  (is (= "foo" (render-obj "foo")))
  (is (= "foo" (render-obj :foo)))
  (is (= "foo&amp;" (render-obj "foo&")))
  (is (= "foo&" (render-obj (keyword "foo&"))))
  (is (= "" (render-obj "")))
  (is (= "" (render-obj nil))))

(deftest test-xml-attribute
  (is (= [" " "foo" "=\"" "bar" "\""] (xml-attribute "foo" "bar")))
  (is (= [" " "foo" "=\"" "bar" "\""] (xml-attribute :foo "bar")))
  (is (= [" " "foo" "=\"" "bar&amp;" "\""] (xml-attribute :foo "bar&")))
  (is (= [" " "foo&amp;" "=\"" "bar" "\""] (xml-attribute "foo&" "bar")))
  (is (= [" " "foo" "=\"" "bar&" "\""] (xml-attribute "foo" (keyword "bar&")))))

(deftest test-render-attribute
  (is (= [" " "foo" "=\"" "bar" "\""] (render-attribute ["foo" "bar"])))
  (is (= [" " "foo" "=\"" "foo" "\""] (render-attribute ["foo" true])))
  (is (= [""] (render-attribute ["foo" nil])))
  (is (= [""] (render-attribute nil))))

(deftest test-render-attr-map
  (is (= [" " "foo" "=\"" "bar" "\""] (render-attr-map { :foo "bar" })))
  (is (= [" " "foo" "=\"" "bar" "\"" " " "baz" "=\"" "biz" "\""] (render-attr-map { :foo "bar" :baz "biz" })))
  (is (= [] (render-attr-map {})))
  (is (= [] (render-attr-map nil))))

(deftest test-normalize-element
  (is (= ["test" {}] (normalize-element [:test])))
  (is (= ["test" { :class "blah" }] (normalize-element [:test.blah])))
  (is (= ["test" { :id "blah" }] (normalize-element [:test#blah])))
  (is (= ["test" { :id "blah" :style "foo" }] (normalize-element [:test#blah { :style "foo" }])))
  (is (= ["test" { :id "blah" :style "foo" } [[:content "blah blah blah"]]]
         (normalize-element [:test#blah { :style "foo" } [:content "blah blah blah"]]))))

(deftest test-render-xml
  (is (= ["foo"] (render-xml "foo")))
  (is (= ["foo"] (render-xml :foo)))
  (is (= ["<p>test</p>"] (render-xml (keyword "<p>test</p>"))))
  (is (= ["1"] (render-xml 1)))
  (is (= ["foo" "1"] (render-xml (list "foo" 1))))
  (is (= ["<" "foo" " " "style" "=\"" "blah&amp;" "\"" ">" "1" "</" "foo" ">"] (render-xml [:foo { :style "blah&" } 1])))
  (is (= ["<" "foo" " " "style" "=\"" "blah&amp;" "\"" ">" "<p>test</p>" "</" "foo" ">"]
         (render-xml [:foo { :style "blah&" } (keyword "<p>test</p>")])))
  (is (= [""] (render-xml nil))))