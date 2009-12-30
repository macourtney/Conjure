(ns test.util.test-session-utils
  (:use clojure.contrib.test-is
        conjure.util.session-utils))
  
(deftest test-create-session-id
  (is (create-session-id))
  (is (not (= (create-session-id) (create-session-id)))))

(deftest test-session-id
  (is (= (session-id { :params {:session-id "blah" } }) "blah"))
  (is (= (session-id { :headers { "cookie" (str session-id-name "=blah") } }) "blah"))
  (is (nil? (session-id { :params { } })))
  (is (nil? (session-id { }))))
  
(deftest test-manage-session
  (let [response-map (manage-session {} {})]
    (is (:headers response-map))
    (is (get (:headers response-map) "Set-Cookie")))
  (let [response-map (manage-session { :headers { "cookie" "blah" } } {})]
    (is (nil? (:headers response-map))))
  (let [response-map (manage-session {} { :headers { "Set-Cookie" "blah" } })]
    (is (:headers response-map))
    (is (= (get (:headers response-map) "Set-Cookie") "blah"))))