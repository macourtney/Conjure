(ns test.util.test-loading-utils
  (:import [java.io File Reader])
  (:use clojure.contrib.test-is
        conjure.util.loading-utils))
        
(deftest test-system-class-loader
  (let [test-class-loader (system-class-loader)]
    (is (not (nil? test-class-loader)))
    (is (instance? ClassLoader test-class-loader))))
    
;(deftest test-resource-reader
;  (let [parent-directory "conjure/util"
;        file-name "loading_utils.clj"
;        reader (resource-reader parent-directory file-name)]
;    (is (not (nil? reader)))
;    (is (instance? Reader reader))
;    (is (thrown? RuntimeException (resource-reader parent-directory "fail.txt")))))

;(deftest test-get-classpath-dir-ending-with
;  (let [vendor-directory (get-classpath-dir-ending-with "vendor")]
;    (is (not (nil? vendor-directory)))
;    (is (instance? File vendor-directory))))

(deftest test-dashes-to-underscores
  (is (= (dashes-to-underscores "test") "test"))
  (is (= (dashes-to-underscores "test-this") "test_this"))
  (is (= (dashes-to-underscores "test-this-now") "test_this_now"))
  (is (= (dashes-to-underscores "test_this") "test_this"))
  (is (= (dashes-to-underscores "") ""))
  (is (= (dashes-to-underscores nil) nil)))
  
(deftest test-underscores-to-dashes
  (is (= (underscores-to-dashes "test") "test"))
  (is (= (underscores-to-dashes "test_this") "test-this"))
  (is (= (underscores-to-dashes "test_this_now") "test-this-now"))
  (is (= (underscores-to-dashes "test-this") "test-this"))
  (is (= (underscores-to-dashes "") ""))
  (is (= (underscores-to-dashes nil) nil)))
  
(deftest test-slashes-to-dots
  (is (= (slashes-to-dots "test") "test"))
  (is (= (slashes-to-dots "test/this") "test.this"))
  (is (= (slashes-to-dots "test\\this") "test.this"))
  (is (= (slashes-to-dots "test/this/now") "test.this.now"))
  (is (= (slashes-to-dots "test\\this\\now") "test.this.now"))
  (is (= (slashes-to-dots "test.this") "test.this"))
  (is (= (slashes-to-dots "") ""))
  (is (= (slashes-to-dots nil) nil)))
  
(deftest test-clj-file-to-symbol-string
  (is (= (clj-file-to-symbol-string "test.clj") "test"))
  (is (= (clj-file-to-symbol-string "test_this.clj") "test-this"))
  (is (= (clj-file-to-symbol-string "test_this_now.clj") "test-this-now"))
  (is (= (clj-file-to-symbol-string "test-this.clj") "test-this"))
  (is (= (clj-file-to-symbol-string "test_this") "test-this"))
  (is (= (clj-file-to-symbol-string "") ""))
  (is (= (clj-file-to-symbol-string nil) nil)))
  
(deftest test-symbol-string-to-clj-file
  (is (= (symbol-string-to-clj-file "test") "test.clj"))
  (is (= (symbol-string-to-clj-file "test-this") "test_this.clj"))
  (is (= (symbol-string-to-clj-file "test-this-now") "test_this_now.clj"))
  (is (= (symbol-string-to-clj-file "test_this") "test_this.clj"))
  (is (= (symbol-string-to-clj-file "") ""))
  (is (= (symbol-string-to-clj-file nil) nil)))
  
(deftest test-namespace-string-for-file
  (is (= (namespace-string-for-file "test/util" "test_loading_utils.clj") "test.util.test-loading-utils"))
  (is (= (namespace-string-for-file nil "test_loading_utils.clj") "test-loading-utils"))
  (is (= (namespace-string-for-file "test/util" nil) nil)))