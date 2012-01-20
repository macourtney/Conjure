(ns views.templates.test-record-form
  (:use clojure.test
        views.templates.record-form)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [drift-db.core :as drift-db]))

(def test-column-metadata-1 { :auto-increment true, :default "(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D36D2960_AD38_447B_A6F4_4F202EE4E709)", :length 10, :not-null true, :primary-key true, :name :id, :type :integer })
(def test-column-metadata-2 { :length 255, :name :text, :type :string })

(def test-table-metadata { :name "messages", :columns (list test-column-metadata-1 test-column-metadata-2) })

(def test-record { :id 1, :text "blah"})

(deftest test-editor
  (is (editor test-record :id))
  (is (editor test-record :text))
  (is (nil? (editor test-record nil)))
  (is (nil? (editor nil :text)))
  (is (nil? (editor nil nil))))

(deftest test-form-row
  (is (nil? (form-row test-record test-column-metadata-1)))
  (is (= [:p [:strong (conjure-str-utils/human-title-case (drift-db/column-name test-column-metadata-2))] ": "
          (editor test-record (drift-db/column-name test-column-metadata-2))]
         (form-row test-record test-column-metadata-2 ))))

(deftest test-render-body
  (is (= [(form-row test-record test-column-metadata-1) (form-row test-record test-column-metadata-2)]
         (render-body test-table-metadata test-record))))