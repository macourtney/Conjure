(ns test.config.test-routes
  (:use clojure.contrib.test-is
        routes))

(def controller-name "test")
(def action-name "show")
(def id "1")

(deftest test-draw
  (let [routes-seq (draw)
        route-fn (first routes-seq)]
    (is (=
      { :action action-name, :controller controller-name, :params { :id id } }
      (route-fn (str "/" controller-name "/" action-name "/" id))))
    (is (=
      { :action action-name, :controller controller-name, :params {} }
      (route-fn (str "/" controller-name "/" action-name))))
    (is (=
      { :action action-name, :controller controller-name, :params {} }
      (route-fn (str "/" controller-name "/" action-name "/"))))
    (is (=
      { :action "index", :controller controller-name, :params {} }
      (route-fn (str "/" controller-name))))
    (is (=
      { :action "index", :controller controller-name, :params {} }
      (route-fn (str "/" controller-name "/"))))
    (is (=
      { :action "index", :controller "home", :params {} }
      (route-fn "/")))))