(ns conjure.core.plugin.test-builder
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.core.plugin.builder)
  (:require [conjure.core.plugin.util :as plugin-util]))

(def test-plugin-name "test-plugin")

(defn verify-and-delete [& files-or-directories]
  (doseq [file-or-directory files-or-directories]
    (is (.exists file-or-directory)))
  (doseq [file-or-directory files-or-directories]
    (is (.delete file-or-directory))
    (is (not (.exists file-or-directory)))))

(deftest test-find-or-create-plugin-directory
  (let [test-plugin-name "test-plugin"
        plugin-directory (find-or-create-plugin-directory test-plugin-name)]
    (is (not (nil? plugin-directory)))
    (is (instance? File plugin-directory))
    (verify-and-delete plugin-directory)))

(deftest test-create-plugin-subdirectory
  (let [plugin-directory (plugin-util/plugin-directory test-plugin-name)
        test-subdirectory-name "test-subdirectory"
        test-subdirectory (File. plugin-directory test-subdirectory-name)]
    (is (not (.exists plugin-directory)))
    (find-or-create-plugin-directory test-plugin-name)
    (is (not (.exists test-subdirectory)))
    (create-plugin-subdirectory plugin-directory test-subdirectory-name)
    (verify-and-delete test-subdirectory plugin-directory)))

(deftest test-create-test-directory
  (let [plugin-directory (plugin-util/plugin-directory test-plugin-name)
        test-directory (File. plugin-directory plugin-util/test-directory-name)]
    (is (not (.exists plugin-directory)))
    (find-or-create-plugin-directory test-plugin-name)
    (is (not (.exists test-directory)))
    (create-test-directory plugin-directory)
    (verify-and-delete test-directory plugin-directory)))

(deftest test-create-plugin-files
  (let [plugin-directory (plugin-util/plugin-directory test-plugin-name)
        test-directory (File. plugin-directory plugin-util/test-directory-name)
        plugin-script-file (File. plugin-directory plugin-util/plugin-file-name)]
    (is (not (.exists plugin-directory)))
    (is (not (.exists test-directory)))
    (is (not (.exists plugin-script-file)))
    (create-plugin-files { :name test-plugin-name })
    (verify-and-delete test-directory plugin-script-file plugin-directory)))