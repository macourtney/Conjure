(defproject org.conjure/conjure-test "1.0.0"
  :description "A collection of test utilities for conjure."

  :dependencies [[clojure-tools "1.1.2"]
                 [drift "1.4.5"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.conjure/conjure-config "1.0.0"]
                 [org.conjure/conjure-server "1.0.0"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-flow "1.0.0"]
                                    [org.conjure/conjure-model "1.0.0"]
                                    [org.drift-db/drift-db-h2 "1.1.4"]] } })