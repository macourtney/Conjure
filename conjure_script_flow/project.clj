(defproject org.conjure/conjure-script-flow "1.0.1-SNAPSHOT"
  :description "The generator and destroyers for the flow files."
  :dependencies [[org.conjure/conjure-flow "1.0.1-SNAPSHOT"]
                 [org.conjure/conjure-test "1.0.1-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "1.0.1-SNAPSHOT"]] } })