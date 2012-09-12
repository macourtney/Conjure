(defproject org.conjure/conjure-test "0.9.0-SNAPSHOT"
  :description "A collection of test utilities for conjure."

  :dependencies [[clojure-tools "1.1.2-SNAPSHOT"]
                 [drift "1.4.3"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-server "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.16"]
                                    [org.conjure/conjure-flow "0.9.0-SNAPSHOT"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                                    [org.drift-db/drift-db-h2 "1.1.2"]] } })