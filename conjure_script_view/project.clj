(defproject org.conjure/conjure-script-view "0.9.0-SNAPSHOT"
  :description "The generator and destroyers for the view files."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-view "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-test "0.9.0-SNAPSHOT"]]
  
  :profiles { :dev { :dependencies [[org.conjure/conjure-model "0.9.0-SNAPSHOT"]] } })