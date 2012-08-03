(defproject org.conjure/conjure-script-core "0.9.0-SNAPSHOT"
  :description "The core namespaces for conjure script."
  :dependencies [[clojure-tools "1.1.1-SNAPSHOT"]
                 [org.conjure/conjure-plugin "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-script-plugin "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-server "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.16"]
                                    [org.drift-db/drift-db-h2 "1.1.2"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]] } })