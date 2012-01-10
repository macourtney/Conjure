(ns test-helper
  (:import [java.io File])
  (:use clojure.test))
  
(defn 
#^{:doc "Verifies the given file is not nil, is an instance of File, and has the given name."}
  test-file [file expected-file-name]
  (is (not (nil? file)))
  (is (instance? File file))
  (is (and file (= expected-file-name (.getName file)))))
  
(defn
#^{:doc "Simply calls test-file on the given directory and name."}
  test-directory [directory expected-directory-name]
  (test-file directory expected-directory-name))