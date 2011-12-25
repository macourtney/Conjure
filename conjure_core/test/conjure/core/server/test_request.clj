(ns conjure.core.server.test-request
  (:import [java.io ByteArrayInputStream])
  (:use clojure.test
        conjure.core.server.request))

(def controller-name "test")
(def action-name "show")

(deftest test-parameters
  (let [params { :id 1 }]
    (set-request-map { :params params }
      (is (= params (parameters))))))

(deftest test-id
  (let [test-id 1]
    (set-request-map { :params { :id test-id } }
      (is (= test-id (id)))))
  (let [test-id { :id 1 }]
    (set-request-map { :params { :id test-id } }
      (is (= test-id (id))))))

(deftest test-id-str
  (let [test-id "1"]
    (set-request-map { :params { :id test-id } }
      (is (= test-id (id-str))))
    (set-request-map { :params { :id { :id test-id } } }
      (is (= test-id (id-str))))))

(defn
#^{ :doc "Converts the given string into an input stream. Assumes the character incoding is UTF-8." }
  string-as-input-stream [string]
  (new ByteArrayInputStream (. string getBytes "UTF-8")))

(deftest test-augment-params
  (is (= {:params { :foo "bar" } } (augment-params { :params {} } { :foo "bar" })))
  (is (= {:params { :foo "bar", :biz "baz" } } (augment-params { :params { :biz "baz" } } { :foo "bar" })))
  (is (= {:params { :foo "bar" } } (augment-params { :params { :foo "bar" } } {})))
  (is (= {:params { :foo "bar" } } (augment-params { :params { :foo "bar" } } nil))))
  
(deftest test-parse-post-params
  (is (= 
    { :foo "bar" }
    (parse-post-params
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar") } })))
  (is (= 
    { :foo "bar", :biz "baz" }
    (parse-post-params
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 15,
          :body (string-as-input-stream "foo=bar&biz=baz") } })))
  (is (= 
    {} 
    (parse-post-params 
      { :request-method :get, 
        :content-length 7, 
        :body (string-as-input-stream "foo=bar") })))
  (is (= 
    {} 
    (parse-post-params 
      { :request-method :post,
        :content-type "multipart/form-data",
        :content-length 7, 
        :body (string-as-input-stream "foo=bar") })))
  (is (nil? (augment-params nil { :foo "bar" } ))))

(deftest test-parse-params
  (is (= 
    { :foo "bar", :biz "baz" }
    (parse-params 
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar"),
          :query-string "biz=baz" } })))
  (is (= { :biz "baz" } (parse-params { :request { :query-string "biz=baz" } } )))
  (is (= 
    { :foo "bar" } 
    (parse-params 
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar") } } )))
  (is (= {} (parse-params {} ))))

(deftest test-update-request-map
  (let [headers { "cookie" "SID=blah" }]
    (let [request-map (update-request-map nil)]
      (is (= nil (:params request-map))))
      
    (is (=
      { :params { :foo "bar" },
        :request 
        { :uri "",
          :query-string "foo=bar"
          :headers headers } }
      (update-request-map 
        { :request 
          { :uri "",
            :query-string "foo=bar"
            :headers headers } })))
          
    (let [uri (str "/" controller-name "/" action-name "/1")]
      (is (= 
        { :params { :foo "bar" }, 
          :request 
          { :uri uri, 
            :query-string "foo=bar"
            :headers headers } }
        (update-request-map 
          { :request 
            { :uri uri,
              :query-string "foo=bar"
              :headers headers } }))))))