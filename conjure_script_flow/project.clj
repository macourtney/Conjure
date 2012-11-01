(defproject org.conjure/conjure-script-flow "1.0.0"
  :description "The generator and destroyers for the flow files."
  :dependencies [[org.conjure/conjure-flow "1.0.0"]
                 [org.conjure/conjure-test "1.0.0"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "1.0.0"]] } })