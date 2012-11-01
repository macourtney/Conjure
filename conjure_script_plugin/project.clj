(defproject org.conjure/conjure-script-plugin "1.0.0"
  :description "The generator and destroyers for the plugin files."

  :dependencies [[clojure-tools "1.1.2"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.conjure/conjure-plugin "1.0.0"]
                 [org.conjure/conjure-test "1.0.0"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "1.0.0"]] } })