(defproject org.conjure/conjure-model "0.9.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clj-record "1.1.0"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]
                 [org.drift-db/drift-db "1.1.3"]]
  
  :profiles { :dev { :dependencies [[org.drift-db/drift-db-h2 "1.1.3"]] } })