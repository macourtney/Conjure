(ns conjure.core.controller.test-base
  (:use clojure.test
        conjure.core.controller.base)
  (:require [conjure.core.controller.util :as controller-util]
            [conjure.core.server.request :as request]
            [conjure.core.util.session-utils :as session-utils]))

(defn
#^{:doc "Returns a redirect response map based on the given url and status, for use when testing."}
  redirect-map 
  ([url] (redirect-map url 302))
  ([url status] 
    { :status status, 
      :headers { "Location" url, "Connection" "close" }, 
      :body (str "<html><body>You are being redirected to <a href=\"" url "\">" url "</a></body></html>") }))

(deftest test-redirect-to-full-url
  (let [url "http://example.com/home"]
    (is (= (redirect-map url) (redirect-to-full-url url)))
    (is (= (redirect-map url 301) (redirect-to-full-url url 301)))))

(deftest test-redirect-to
  (is (= (redirect-map "http://www.conjureapp.com") (redirect-to "http://www.conjureapp.com")))
  (request/set-request-map { :scheme :http, :server-name "www.conjureapp.com" }
    (is (= 
      (redirect-map "/home/welcome") 
      (redirect-to "/home/welcome"))))
  (request/set-request-map { :request { :scheme :http, :server-name "www.conjureapp.com" } :controller "home" :action "welcome" }
    (is (= 
      (redirect-map "http://www.conjureapp.com/home/welcome") 
      (redirect-to {})))
    (is (= 
      (redirect-map "http://www.conjureapp.com/home/goodbye") 
      (redirect-to { :controller "home", :action "goodbye" } )))
    (is (= 
      (redirect-map "http://www.conjureapp.com/home/goodbye" 301)
      (redirect-to { :controller "home", :action "goodbye", :status 301 })))))