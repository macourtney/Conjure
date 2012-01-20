(ns views.templates.test-record-view
  (:use clojure.test
        views.templates.record-view)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.core.view.base :as view-base]))

(def test-column-metadata-1 { :auto-increment true, :default "(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_D36D2960_AD38_447B_A6F4_4F202EE4E709)", :length 10, :not-null true, :primary-key true, :name :id, :type :integer })
(def test-column-metadata-2 { :length 255, :name :text, :type :string })
(def test-column-metadata-3 { :name :user-id, :type :integer })

(def test-table-metadata { :name "messages", :columns (list test-column-metadata-1 test-column-metadata-2 test-column-metadata-3) })

(def test-record { :id 1, :text "blah", :user-id 1 })

(deftest test-view-id-row
  (let [user-key-str (name (:name test-column-metadata-3))
        belongs-to-model (conjure-str-utils/strip-ending user-key-str "-id")
        field-name (conjure-str-utils/human-title-case belongs-to-model)]
    (is (= [:p [:strong field-name] ": " 
              (view-base/link-to (:user-id test-record)
                { :controller belongs-to-model, 
                  :action "show", 
                  :params { :id (:user-id test-record) } })]
           (view-id-row test-record (:name test-column-metadata-3)))))
  (is (nil? (view-id-row nil (:name test-column-metadata-3))))
  (is (nil? (view-id-row test-record nil)))
  (is (nil? (view-id-row nil nil))))

(deftest test-view-row
  (is (= [:p [:strong (conjure-str-utils/human-title-case (name (:name test-column-metadata-1)))] ": "
           (:id test-record)]
         (view-row test-record test-column-metadata-1)))
  (is (= [:p [:strong (conjure-str-utils/human-title-case (name (:name test-column-metadata-2)))] ": "
           (:text test-record)]
         (view-row test-record test-column-metadata-2)))
  (is (= (view-id-row test-record (:name test-column-metadata-3)) (view-row test-record test-column-metadata-3)))
  (is (nil? (view-row nil test-column-metadata-1)))
  (is (nil? (view-row test-record nil)))
  (is (nil? (view-row nil nil))))

(deftest test-render-body
  (is (= [(view-row test-record test-column-metadata-1) (view-row test-record test-column-metadata-2)
           (view-row test-record test-column-metadata-3)]
         (render-body test-table-metadata test-record))))