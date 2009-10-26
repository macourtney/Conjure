(ns test.util.test-html-utils
  (:use clojure.contrib.test-is
        conjure.util.html-utils))

(deftest test-url-encode
  (is (= "foo+bar" (url-encode "foo bar"))))

(deftest test-url-decode
  (is (= "foo bar" (url-decode "foo+bar"))))

(deftest test-add-param
  (is (= { :foo "bar" } (add-param {} ["foo" "bar"])))
  (is (= { :foo "bar", :baz "biz" } (add-param { :foo "bar" } ["baz" "biz"])))
  (is (= { :foo "bar biz" } (add-param {} ["foo" "bar+biz"]))))

(deftest test-parse-query-params
  (is (= { :foo "bar" } (parse-query-params "foo=bar")))
  (is (= { :foo "bar", :baz "biz" } (parse-query-params "foo=bar&baz=biz")))
  (is (= { :foo { :bar "boz" }, :baz "biz" } (parse-query-params "foo[bar]=boz&baz=biz")))
  (is (= { :foo { :bar "boz", :buz "bez" }, :baz "biz" } (parse-query-params "foo[bar]=boz&baz=biz&foo[buz]=bez")))
  (is (= {} (parse-query-params "")))
  (is (= {} (parse-query-params nil))))

(deftest test-key-seq
  (is (= [] (key-seq "")))
  (is (= [ :foo ] (key-seq "foo")))
  (is (= [ :foo :bar ] (key-seq "foo[bar]")))
  (is (= [ :foo :bar :baz] (key-seq "foo[bar][baz]"))))

(deftest test-update-params
  (is (= {} (update-params {} [] nil)))
  (is (= { :baz "biz" } (update-params {} [:baz] "biz")))
  (is (= { :baz { :boz "biz" } } (update-params {} [:baz :boz] "biz")))
  (is (= { :baz { :boz { :buz "biz" } } } (update-params {} [:baz :boz :buz] "biz")))
  (let [params { :foo "bar", :test { :id 0 } }]
    (is (= params (update-params params [] nil)))
    (is (= (merge params { :baz "biz" }) (update-params params [:baz] "biz")))
    (is (= (merge params { :baz { :boz "biz" } }) (update-params params [:baz :boz] "biz")))
    (is (= (merge params { :baz { :boz { :buz "biz" } } }) (update-params params [:baz :boz :buz] "biz")))
    (is (= (merge params { :test { :id 0, :function "test-fn" } }) (update-params params [:test :function] "test-fn")))
    (is (= (merge params { :test { :id 0, :function { :id 1 } } }) (update-params params [:test :function :id] 1)))))