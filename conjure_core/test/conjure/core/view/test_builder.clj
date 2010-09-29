(ns conjure.core.view.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.core.view.builder)
  (:require [conjure.core.view.util :as util]
            [clojure.tools.loading-utils :as loading-utils]))

(def controller "test-builder")
(def action "show")

(def controller-dir (loading-utils/dashes-to-underscores controller))

(deftest test-find-or-create-controller-directory
  (let [controller-directory 
         (find-or-create-controller-directory 
          { :views-directory (util/find-views-directory),
            :controller controller,
            :silent true })]
    (test-directory controller-directory controller-dir)
    (is (and controller-directory (.delete controller-directory))))
  (let [controller-directory 
         (find-or-create-controller-directory 
         { :controller controller,
           :silent true })]
    (test-directory controller-directory controller-dir)
    (is (and controller-directory (.delete controller-directory))))
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
    (is (and view-file (.delete view-file))))
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