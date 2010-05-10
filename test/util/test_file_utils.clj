(ns test.util.test-file-utils
  (:import [java.io File FileReader])
  (:use clojure.contrib.test-is
        conjure.util.file-utils))
        
(deftest test-user-directory
  (let [user-dir (user-directory)]
    (is (not (nil? user-dir)))
    (is (instance? File user-dir))
    (is (= (. user-dir getName) "test_app"))))
        
(deftest test-find-file
  (let [parent-dir (new File (user-directory) "test/util")]
    (let [test-file-utils-file (find-file parent-dir "test_file_utils.clj")]
      (is (not (nil? test-file-utils-file)))
      (is (instance? File test-file-utils-file))
      (is (= (. test-file-utils-file getName) "test_file_utils.clj")))
    (is (nil? (find-file parent-dir "fail.txt")))
    (is (nil? (find-file (new File "fail-dir") "test_file_utils.clj")))
    (is (thrown? NullPointerException (find-file parent-dir nil)))
    (is (thrown? NullPointerException (find-file nil "test_file_utils.clj")))))
    
(deftest test-find-directory
  (let [parent-dir (new File (user-directory) "test")]
    (let [test-dir (find-directory parent-dir "util")]
      (is (not (nil? test-dir)))
      (is (instance? File test-dir))
      (is (= (. test-dir getName) "util")))
    (is (nil? (find-directory parent-dir "fail")))
    (is (nil? (find-directory (new File "fail-dir") "util")))
    (is (thrown? NullPointerException (find-directory parent-dir nil)))
    (is (thrown? NullPointerException (find-directory nil "util")))))
    
(deftest test-write-file-content
  (let [test-content "Test content."
        test-file (new File (user-directory) "test/util/test.txt")]
    (write-file-content test-file test-content)
    (let [test-file-reader (new FileReader test-file)
          test-file-content (make-array (. Character TYPE) 20)
          chars-read (. test-file-reader read test-file-content)
          test-file-content-str (new String test-file-content 0 chars-read)]
      (is (= test-file-content-str test-content))
      (. test-file-reader close))
    (. test-file delete)))

(deftest test-recursive-delete
  (let [test-dir (new File (user-directory) "delete-test")]
    (.mkdirs (new File test-dir "blah/foo"))
    (is (recursive-delete test-dir))))