(defproject org.conjure/conjure-script-plugin "0.9.0-SNAPSHOT"
  :description "The generator and destroyers for the plugin files."

  :dependencies [[clojure-tools "1.1.1"]
                 [org.clojure/tools.logging "0.2.0"]
                 [org.conjure/conjure-plugin "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-test "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.16"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]] } })