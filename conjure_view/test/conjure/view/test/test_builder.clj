(ns conjure.view.test.test-builder
  (:use test-helper
        clojure.test
        conjure.view.builder)
  (:require [conjure.view.util :as util]
            [clojure.tools.loading-utils :as loading-utils]))

(def service "test-builder")
(def action "show")

(def service-dir (loading-utils/dashes-to-underscores service))

(deftest test-find-or-create-service-directory
  (let [service-directory 
         (find-or-create-service-directory 
          { :views-directory (util/find-views-directory),
            :service service,
            :silent true })]
    (test-directory service-directory service-dir)
    (is (and service-directory (.delete service-directory))))
  (let [service-directory 
         (find-or-create-service-directory 
         { :service service,
           :silent true })]
    (test-directory service-directory service-dir)
    (is (and service-directory (.delete service-directory))))
  (is (nil? (find-or-create-service-directory 
              { :service nil
                :silent true })))
  (is (nil? (find-or-create-service-directory
              { :views-directory nil, 
                :service service, 
                :silent true })))
  (is (nil? (find-or-create-service-directory 
              { :views-directory nil,
                :service nil,
                :silent true }))))

(deftest test-create-view-file
  (let [view-file (create-view-file
                    { :service-directory 
                        (find-or-create-service-directory 
                          { :service service,
                            :silent true }),
                      :action action,
                      :silent true })]
    (test-file view-file (str action ".clj"))
    (is (and view-file (.delete view-file))))
  (is (nil? (create-view-file 
              { :service-directory 
                  (find-or-create-service-directory 
                    { :service service,
                      :silent true }),
                :action nil,
                :silent true })))
  (is (nil? (create-view-file 
              { :service-directory nil,
                :action action
                :silent true })))
  (is (nil? (create-view-file 
              { :service-directory nil,
                :action nil
                :silent true }))))