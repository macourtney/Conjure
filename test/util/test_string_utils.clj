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
   (is (= (str-keyword "test") "test"))
   (is (= (str-keyword nil) nil)))
   
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
  
(deftest test-str-replace-pair
  (is (= (str-replace-pair "foo.bar" ["." " dot "]) "foo dot bar"))
  (is (= (str-replace-pair "foo-bar" ["." " dot "]) "foo-bar"))
  (is (= (str-replace-pair nil ["." " dot "]) nil))
  (is (= (str-replace-pair "foo.bar" nil) "foo.bar"))
  (is (= (str-replace-pair nil nil) nil)))

(deftest test-str-replace-if
  (is (= (str-replace-if "foo.bar" { "." " dot " }) "foo dot bar"))
  (is (= (str-replace-if "me@foo.bar" { "." " dot ", "@" " at " }) "me at foo dot bar"))
  (is (= (str-replace-if "me@foo.bar" { "." " dot ", "@" " at ", "|" " or " }) "me at foo dot bar"))
  (is (= (str-replace-if "me@foo.bar" { }) "me@foo.bar"))
  (is (= (str-replace-if "me@foo.bar" nil) "me@foo.bar"))
  (is (= (str-replace-if nil { "." " dot " }) nil))
  (is (= (str-replace-if nil nil) nil)))

(deftest test-human-readable
  (is (= (human-readable "foo-bar") "foo bar"))
  (is (= (human-readable "foo_bar") "foo bar"))
  (is (= (human-readable "foo_bar-baz") "foo bar baz"))
  (is (= (human-readable "foo") "foo"))
  (is (= (human-readable "") ""))
  (is (= (human-readable nil) nil)))