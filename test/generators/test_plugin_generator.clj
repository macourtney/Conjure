(ns test.generators.test-plugin-generator
  (:use clojure.contrib.test-is
        generators.plugin-generator))

(deftest test-generate-install-function
  (is (generate-install-function)))

(deftest test-generate-uninstall-function
  (is (generate-uninstall-function)))

(deftest test-generate-initialize-function
  (is (generate-initialize-function)))

(deftest test-generate-plugin-content
  (is (generate-plugin-content "test-plugin"))
  (is (generate-plugin-content "test-plugin" (generate-install-function) (generate-uninstall-function) 
        (generate-initialize-function))))

(deftest test-test-file
  (is (test-file "test-plugin")))

(deftest test-generate-test-content
  (is (generate-test-content "test-plugin")))