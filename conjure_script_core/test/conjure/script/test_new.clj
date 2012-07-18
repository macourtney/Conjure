(ns conjure.script.test-new
  (:import [java.io File]
           [java.util.zip ZipEntry])
  (:use clojure.test
        conjure.script.new
        test-helper)
  (:require [clojure.tools.loading-utils :as loading-utils]))

(def plugin-name "test")

(def new-project-test-dir (File. (find-test-directory) "new_project"))

(def conjure-zip-dir "conjure/")
(def file-to-extract "execute.clj")

(deftest test-entry-file
  (is (.exists new-project-test-dir))
  (is (= 
        (.getPath (File. new-project-test-dir "blah.clj"))
        (.getPath (entry-file new-project-test-dir (ZipEntry. (str new-project-zip-directory "blah.clj"))))))
  (is (= 
        (.getPath (File. new-project-test-dir "foo/bar/blah.clj"))
        (.getPath (entry-file new-project-test-dir (ZipEntry. (str new-project-zip-directory "foo/bar/blah.clj")))))))

(defn conjure-core-zip-entries []
  (loading-utils/class-path-zip-entries conjure-zip-dir))

(defn find-test-entry []
  (some #(when (.endsWith (.getName %1) file-to-extract) %1) (conjure-core-zip-entries)))

(deftest test-create-file
  (let [entry-file (File. new-project-test-dir file-to-extract)
        execute-entry (find-test-entry)]
    (is execute-entry)
    (create-file entry-file execute-entry)
    (is (.exists entry-file))
    (is (.delete entry-file)))
  (let [entry-file (File. new-project-test-dir (str "core/" file-to-extract))]
    (create-file  entry-file  (find-test-entry))
    (is (.exists entry-file))
    (is (.delete entry-file))
    (is (.delete (.getParentFile entry-file))))
  (is (empty? (.listFiles new-project-test-dir))))

(deftest test-extract-zip-entry
  (let [zip-entry (find-test-entry)]
    (extract-zip-entry new-project-test-dir zip-entry conjure-zip-dir)
    (let [entry-file (entry-file new-project-test-dir zip-entry conjure-zip-dir)]
      (is (.exists entry-file))
      (is (.isFile entry-file))
      (is (.delete entry-file))))
  (let [new-project-subdirectory (File. new-project-test-dir "core")
        zip-entry (find-test-entry)]
    (extract-zip-entry new-project-subdirectory zip-entry conjure-zip-dir)
    (let [entry-file (entry-file new-project-subdirectory zip-entry conjure-zip-dir)]
      (is (.exists entry-file))
      (is (.isFile entry-file))
      (is (.delete entry-file))
      (is (.delete (.getParentFile entry-file)))))
  (is (empty? (.listFiles new-project-test-dir))))

(deftest test-extract-all
  (let [conjure-core-zip-dir "conjure/server/"
        zip-entries (loading-utils/class-path-zip-entries conjure-core-zip-dir)]
    (extract-all new-project-test-dir zip-entries conjure-core-zip-dir)
    (doseq [zip-entry zip-entries]
      (let [entry-file (entry-file new-project-test-dir zip-entry conjure-core-zip-dir)]
        (is (.exists entry-file))
        (is (.isFile entry-file))
        (is (.delete entry-file))))
    (is (empty? (.listFiles new-project-test-dir)))))

(deftest test-set-database
  (let [config-file (File. new-project-test-dir "/src/config/db_config.clj")]
    (.mkdirs (.getParentFile config-file))
    (spit config-file (str "Blah blah blah " default-database " blah"))
    (set-database "mysql" new-project-test-dir)
    (is (.exists config-file))
    (is (= "Blah blah blah mysql blah" (slurp config-file)))
    (is (.delete config-file))
    (is (.delete (.getParentFile config-file)))
    (is (.delete (.getParentFile (.getParentFile config-file))))
    (is (empty? (.listFiles new-project-test-dir)))))