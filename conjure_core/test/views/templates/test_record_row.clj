(ns views.templates.test-record-row
  (:use clojure.test
        views.templates.record-row)
  (:require [clojure.tools.string-utils :as conjure-str-utils]))

(def test-column-metadata-1 { :auto-increment true, :default "(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D36D2960_AD38_447B_A6F4_4F202EE4E709)", :length 10, :not-null true, :primary-key true, :name :id, :type :integer })
(def test-column-metadata-2 { :length 255, :name :text, :type :string })

(def test-table-metadata { :name "messages", :columns (list test-column-metadata-1 test-column-metadata-2) })

(deftest test-column-names
  (is (= [(:name test-column-metadata-1) (:name test-column-metadata-2)] (column-names test-table-metadata)))
  (is (= [] (column-names nil))))

(deftest test-render-body
  (is (render-body :message test-table-metadata { :id 1, :text "blah"})))