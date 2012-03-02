(defproject org.conjure/conjure-bind "0.9.0-SNAPSHOT"
  :description "Conjure bind is a library to bind data from a model into a view."
  :dependencies [[clojure-tools "1.1.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/clojure "1.3.0"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-view "0.9.0-SNAPSHOT"]]
  :dev-dependencies [[org.clojure/clojure "1.2.1"]
                     [org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                     [org.drift-db/drift-db-h2 "1.0.7"]])