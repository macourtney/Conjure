(defproject org.conjure/conjure-flow "0.9.0-SNAPSHOT"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.16"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                                    [org.conjure/conjure-view "0.9.0-SNAPSHOT"]
                                    [org.drift-db/drift-db "1.1.2"]
                                    [org.drift-db/drift-db-h2 "1.1.2"]] } })