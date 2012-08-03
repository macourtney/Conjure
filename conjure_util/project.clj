(defproject org.conjure/conjure-util "0.9.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clojure-tools "1.1.1-SNAPSHOT"]
                 [org.clojure/tools.namespace "0.1.0"]]
  
  :profiles { :dev { :dependencies [[org.conjure/conjure-model "0.9.0-SNAPSHOT"]] } }

  :aot [conjure.execute])