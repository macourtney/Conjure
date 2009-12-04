(ns test.view.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.view.builder)
  (:require [conjure.view.util :as util]))

(def controller "test")
(def action "show")

(deftest test-find-or-create-controller-directory
  (let [controller-directory 
         (find-or-create-controller-directory 
          { :views-directory (util/find-views-directory),
            :controller controller,
            :silent true })]
    (test-directory controller-directory controller)
    (is (. controller-directory delete)))
  (let [controller-directory 
         (find-or-create-controller-directory 
         { :controller controller,
           :silent true })]
    (test-directory controller-directory controller)
    (is (. controller-directory delete)))
  (is (nil? (find-or-create-controller-directory 
              { :controller nil
                :silent true })))
  (is (nil? (find-or-create-controller-directory
              { :views-directory nil, 
                :controller controller, 
                :silent true })))
  (is (nil? (find-or-create-controller-directory 
              { :views-directory nil,
                :controller nil,
                :silent true }))))

(deftest test-create-view-file
  (let [view-file (create-view-file
                    { :controller-directory 
                        (find-or-create-controller-directory 
                          { :controller controller,
                            :silent true }),
                      :action action,
                      :silent true })]
    (test-file view-file (str action ".clj"))
    (is (. view-file delete)))
  (is (nil? (create-view-file 
              { :controller-directory 
                  (find-or-create-controller-directory 
                    { :controller controller,
                      :silent true }),
                :action nil,
                :silent true })))
  (is (nil? (create-view-file 
              { :controller-directory nil,
                :action action
                :silent true })))
  (is (nil? (create-view-file 
              { :controller-directory nil,
                :action nil
                :silent true }))))