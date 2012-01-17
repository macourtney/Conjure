(ns conjure.script.test-server
  (:use clojure.test
        conjure.script.server))

(deftest test-parse-arguments
  (is (parse-arguments []))
  (is (= [ { :mode "development" } [] "Usage:\n\n Switches    Default  Desc                                                            \n --------    -------  ----                                                            \n -m, --mode           The server mode. For example, development, production, or test. \n" ] (parse-arguments ["-m" "development"]))))