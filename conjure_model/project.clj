(defproject org.conjure/conjure-model "1.0.0"
  :description "FIXME: write description"
  :dependencies [[clj-record "1.1.0"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.conjure/conjure-config "1.0.0"]
                 [org.conjure/conjure-util "1.0.0"]
                 [org.drift-db/drift-db "1.1.4"]]
  
  :profiles { :dev { :dependencies [[org.drift-db/drift-db-h2 "1.1.4"]] } })