(defproject org.conjure/conjure-script-core "1.0.0"
  :description "The core namespaces for conjure script."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-plugin "1.0.0"]
                 [org.conjure/conjure-script-plugin "1.0.0"]
                 [org.conjure/conjure-server "1.0.0"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.drift-db/drift-db-h2 "1.1.4"]
                                    [org.conjure/conjure-model "1.0.0"]] } })