(ns conjure.server.test-server
  (:import [java.io ByteArrayInputStream])
  (:use clojure.test
        conjure.server.server))

(def controller-name "test")
(def action-name "show")

(deftest test-process-request
  (process-request { :controller controller-name, :action action-name }))
  
(deftest test-http-config
  (is (not (nil? (http-config)))))

(deftest test-parse-arguments
  (is (parse-arguments []))
  (is (= [ { :mode "development" } [] "Usage:\r\n\r\n Switches    Default  Desc                                                            \r\n --------    -------  ----                                                            \r\n -m, --mode           The server mode. For example, development, production, or test. \r\n" ] (parse-arguments ["-m" "development"]))))