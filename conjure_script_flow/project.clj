(defproject org.conjure/conjure-script-flow "0.9.0-SNAPSHOT"
  :description "The generator and destroyers for the flow files."
  :dependencies [[org.conjure/conjure-flow "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-test "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]] } })