(ns test.util.test-string-utils
  (:use clojure.contrib.test-is
        conjure.util.string-utils))

(deftest test-prefill
  (is (= (prefill "Blah" 4 "!") "Blah"))
  (is (= (prefill "Blah" 5 "!") "!Blah"))
  (is (= (prefill "Blah" 6 "!") "!!Blah"))
  (is (= (prefill "Blah" 7 "!") "!!!Blah"))
  (is (= (prefill "Blah" -1 "!") "Blah"))
  (is (= (prefill nil 6 "!") "!!!!!!"))
  (is (= (prefill "Blah" 6 nil) "Blah"))
  (is (= (prefill "Blah" nil "!") "Blah")))

(deftest test-str-keyword
   (is (= (str-keyword :test) "test"))
   (is (= (str-keyword "test") "test")))
   
(deftest test-strip-ending
   (is (= (strip-ending "Blah" "h") "Bla"))
   (is (= (strip-ending "Blah" "ah") "Bl"))
   (is (= (strip-ending "Blah" "lah") "B"))
   (is (= (strip-ending "Blah" "Blah") ""))
   (is (= (strip-ending "Blah" "") "Blah"))
   (is (= (strip-ending "Blah" nil) "Blah"))
   (is (= (strip-ending "" "Blah") ""))
   (is (= (strip-ending nil "Blah") nil))
   (is (= (strip-ending nil nil) nil)))
   
(deftest test-add-ending-if-absent
  (is (= (add-ending-if-absent "blah" ".foo") "blah.foo"))
  (is (= (add-ending-if-absent "blah.foo" ".foo") "blah.foo"))
  (is (= (add-ending-if-absent "blah" nil) "blah"))
  (is (= (add-ending-if-absent nil ".foo") ".foo"))
  (is (= (add-ending-if-absent "blah" "") "blah"))
  (is (= (add-ending-if-absent "" ".foo") ".foo")))   