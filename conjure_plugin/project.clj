(defproject org.conjure/conjure-plugin "0.9.0-SNAPSHOT"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[clojure-tools "1.1.1"]
                 [org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.logging "0.2.0"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]]
  :dev-dependencies [[org.clojure/clojure "1.2.1"]
                     [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                     [org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                     [org.conjure/conjure-server "0.9.0-SNAPSHOT"]
                     [org.drift-db/drift-db-h2 "1.0.7"]])