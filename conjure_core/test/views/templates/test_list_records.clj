(ns views.templates.test-list-records
  (:use clojure.test
        views.templates.list-records)
  (:require [clojure.tools.string-utils :as conjure-str-utils]))

(def test-column-metadata-1 { :auto-increment true, :default "(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D36D2960_AD38_447B_A6F4_4F202EE4E709)", :length 10, :not-null true, :primary-key true, :name :id, :type :integer })
(def test-column-metadata-2 { :length 255, :name :text, :type :string })

(def test-table-metadata { :name "messages", :columns (list test-column-metadata-1 test-column-metadata-2) })

(deftest test-header-name
  (is (= [:th (conjure-str-utils/human-title-case (name (:name test-column-metadata-2)))]
         (header-name test-column-metadata-2)))
  (is (nil? (header-name nil))))

(deftest test-header
  (is (= [:tr [(header-name test-column-metadata-1) (header-name test-column-metadata-2)] [:th]]
         (header test-table-metadata)))
  (is (nil? (header nil))))

(deftest test-render-body
  (is (render-body :message test-table-metadata [{ :id 1, :text "blah"}])))