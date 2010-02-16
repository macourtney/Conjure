(ns test.util.test-html-utils
  (:import [java.util Calendar])
  (:use clojure.contrib.test-is
        conjure.util.html-utils))

(deftest test-url-encode
  (is (= "foo+bar" (url-encode "foo bar"))))

(deftest test-url-decode
  (is (= "foo bar" (url-decode "foo+bar"))))

(deftest test-add-param
  (is (= { :foo "bar" } (add-param {} ["foo" "bar"])))
  (is (= { :foo "bar", :baz "biz" } (add-param { :foo "bar" } ["baz" "biz"])))
  (is (= { :foo "bar biz" } (add-param {} ["foo" "bar+biz"])))
  (is (= { :foo { :bar "boz" } } (add-param {} ["foo%5Bbar%5D" "boz"]))))

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

(deftest test-url-param-str
  (is (= "?foo=bar" (url-param-str { :foo "bar" })))
  (is (= "?foo=bar&baz=biz" (url-param-str { :foo "bar", :baz :biz })))
  (is (= "?foo=bar&baz=biz&boz=buz" (url-param-str { :foo "bar", :baz :biz, "boz" "buz" })))
  (is (= "?foo=bar" (url-param-str { :foo "bar", :baz nil })))
  (is (nil? (url-param-str {})))
  (is (nil? (url-param-str nil))))
  
(deftest test-full-url
  (is (= "http://example.com/home/index" (full-url "home/index" "http://example.com/")))
  (is (= "http://www.example.com/home/index" (full-url "http://www.example.com/home/index" "http://example.com/"))))

(deftest test-attribute-str
  (is (= "foo=\"bar\"" (attribute-str "foo" "bar")))
  (is (= "foo=\"&quot;bread&quot; &amp; &quot;butter&quot;\"" (attribute-str "foo" "\"bread\" & \"butter\""))))

(deftest test-attribute-list-str
  (is (= "foo=\"bar\"" (attribute-list-str { :foo "bar" })))
  (is (= "baz=\"biz\" foo=\"bar\"" (attribute-list-str { :baz "biz", :foo "bar" })))
  (is (= "baz=\"biz\" boz=\"buz\" foo=\"bar\"" (attribute-list-str { :baz "biz", :boz "buz", :foo "bar" })))
  (is (= "" (attribute-list-str { })))
  (is (= "" (attribute-list-str nil))))

(deftest test-format-cookie-date
  (is (= "Tue, 16-Feb-2010 10:30:25 GMT" 
    (format-cookie-date 
      (.getTime 
        (doto (. Calendar getInstance) 
          (.set 2010 1 16 10 30 25)))))))

(deftest test-multipart-form-part
  (is (= { "Content-Disposition" { "filename" "file1.txt", "name" "files", "form-data" nil },
           "Content-Type" { "text/plain" nil }
           :data "Blah" } 
    (multipart-form-part 
      "Content-Disposition: form-data; name=\"files\"; filename=\"file1.txt\"\r\nContent-Type: text/plain\r\n\r\nBlah")))
  (is (= { "Content-Disposition" { "form-data" nil, "name" "submit-name" },
           :data "Larry" } 
    (multipart-form-part 
      "Content-Disposition: form-data; name=\"submit-name\"\r\n\r\nLarry"))))

(deftest test-multipart-form-data
  (is (= 
    [ { :data "Larry"
        "Content-Disposition" { "form-data" nil, "name" "submit-name" } }
    
      { :data "contents of file1.txt"
        "Content-Disposition" { "form-data" nil, "name" "files", "filename" "file1.txt" }
        "Content-Type" { "text/plain" nil } } ]
    (multipart-form-data 
"Content-Type: multipart/form-data; boundary=AaB03x\r
\r
--AaB03x\r
Content-Disposition: form-data; name=\"submit-name\"\r
\r
Larry\r
--AaB03x\r
Content-Disposition: form-data; name=\"files\"; filename=\"file1.txt\"\r
Content-Type: text/plain\r
\r
contents of file1.txt\r
--AaB03x--" "AaB03x")))
  (is (= 
    [ { :data "Larry"
        "Content-Disposition" { "form-data" nil, "name" "submit-name" } }
    
      { :data 
          [ { :data "contents of file1.txt"
              "Content-Type" { "text/plain" nil }
              "Content-Disposition" { "file" nil, "filename" "file1.txt" } }
              
            { :data "contents of file2.gif"
              "Content-Type" { "image/gif" nil }
              "Content-Transfer-Encoding" { "binary" nil }
              "Content-Disposition" { "file" nil, "filename" "file2.gif" } } ]
        "Content-Type" { "multipart/mixed" nil, "boundary" "BbC04y" }
        "Content-Disposition" { "form-data" nil, "name" "files" } } ]
    (multipart-form-data 
"Content-Type: multipart/form-data; boundary=AaB03x\r
\r
--AaB03x\r
Content-Disposition: form-data; name=\"submit-name\"\r
\r
Larry\r
--AaB03x\r
Content-Disposition: form-data; name=\"files\"\r
Content-Type: multipart/mixed; boundary=BbC04y\r
\r
--BbC04y\r
Content-Disposition: file; filename=\"file1.txt\"\r
Content-Type: text/plain\r
\r
contents of file1.txt\r
--BbC04y\r
Content-Disposition: file; filename=\"file2.gif\"\r
Content-Type: image/gif\r
Content-Transfer-Encoding: binary\r
\r
contents of file2.gif\r
--BbC04y--\r
--AaB03x--" "AaB03x"))))