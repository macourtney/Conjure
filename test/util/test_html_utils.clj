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
  (is (= {} (parse-query-params "")))
  (is (= {} (parse-query-params nil))))