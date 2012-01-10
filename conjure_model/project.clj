(defproject org.conjure/conjure-model "0.9.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-record "1.1.0"]
                 [log4j/log4j "1.2.16"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]
                 [org.drift-db/drift-db "1.0.7-SNAPSHOT"]]
  :dev-dependencies [[org.drift-db/drift-db-h2 "1.0.7-SNAPSHOT"]
                     [org.clojure/clojure "1.2.1"]])