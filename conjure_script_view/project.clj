(defproject org.conjure/conjure-script-view "1.0.0"
  :description "The generator and destroyers for the view files."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-view "1.0.0"]
                 [org.conjure/conjure-test "1.0.0"]]
  
  :profiles { :dev { :dependencies [[org.conjure/conjure-model "1.0.0"]] } })