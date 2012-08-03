(ns conjure.server.test-server
  (:import [java.io ByteArrayInputStream])
  (:use clojure.test
        conjure.server.server))

(def service-name "test")
(def action-name "show")

(deftest test-process-request
  (is (process-request { :request { :uri (str "/" service-name "/" action-name "/1") :query-string "foo=bar&baz=biz" } })))

(deftest test-http-config
  (is (not (nil? (http-config)))))

(deftest test-parse-arguments
  (is (parse-arguments []))
  (is (= [ { :mode "development" } [] "Usage:\n\n Switches    Default  Desc                                                            \n --------    -------  ----                                                            \n -m, --mode           The server mode. For example, development, production, or test. \n" ] (parse-arguments ["-m" "development"]))))