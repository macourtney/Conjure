(defproject org.conjure/conjure-util "0.9.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[clojure-tools "1.1.1-SNAPSHOT"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.namespace "0.1.0"]]
  :dev-dependencies [[org.clojure/clojure "1.2.1"]
                     [org.conjure/conjure-model "0.9.0-SNAPSHOT"]]

  :aot [conjure.execute])