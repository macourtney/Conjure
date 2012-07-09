(ns conjure.view.test.test-util
  (:import [java.io File])
  (:use clojure.test
        conjure.view.util
        test-helper)
  (:require [clojure.tools.logging :as logging]
            [config.session-config :as session-config]
            [conjure.util.request :as request]))

(def action-name "show")
(def controller-name "test")

(use-fixtures :once init-server)

(deftest test-find-views-directory
  (test-directory (find-views-directory) "views"))

(deftest test-view-files
  (doseq [view-file (view-files)]
    (is (.isFile view-file))
    (is (.endsWith (.getName view-file) ".clj"))))

(deftest test-find-controller-directory
  (test-directory (find-controller-directory (find-views-directory) controller-name) controller-name)
  (test-directory (find-controller-directory controller-name) controller-name)
  (is (nil? (find-controller-directory nil))))
  
(deftest test-find-view-file
  (let [controller-directory (find-controller-directory controller-name)]
    (test-file 
      (find-view-file controller-directory action-name) 
      (str action-name ".clj"))
    (is (nil? (find-view-file controller-directory nil)))
    (is (nil? (find-view-file nil action-name)))
    (is (nil? (find-view-file nil nil)))))
    
(deftest test-load-view
  (request/with-controller-action controller-name action-name
    (load-view))
  (load-view controller-name action-name)
  (load-view controller-name action-name true)
  (request/with-controller-action controller-name action-name
    (is ((ns-resolve 'views.test.show 'render-view)))))

(deftest test-clear-loaded-veiws
  (when (empty? @loaded-views)
    (load-view controller-name action-name))
  (clear-loaded-views)
  (is (empty? @loaded-views)))

(deftest test-view-loaded?
  (clear-loaded-views)
  (is (not (view-loaded? controller-name action-name)))
  (load-view controller-name action-name)
  (is (view-loaded? controller-name action-name))
  (is (view-loaded? (keyword controller-name) (keyword action-name))))

(deftest test-request-view-namespace
  (request/with-controller-action controller-name action-name
    (is (= (str "views." controller-name "." action-name) 
         (request-view-namespace))))
  (is (= (str "views." controller-name "." action-name) 
         (request-view-namespace controller-name action-name)))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace "test-foo" "show-foo")))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace "test_foo" "show_foo")))
  (is (nil? (request-view-namespace nil nil))))
  
(deftest test-view-namespace
  (is (nil? (view-namespace nil)))
  (is (= (str "views." controller-name "." action-name) 
         (view-namespace (new File (find-views-directory) (str controller-name "/" action-name ".clj")))))
  (is (= (str "views.my-controller.action-view") 
         (view-namespace (new File (find-views-directory) "my_controller/action_view.clj")))))

(deftest test-all-view-namespaces
  (when-let [view-namespaces (all-view-namespaces)]
    (doseq [view-namespace view-namespaces]
      (is view-namespace))))