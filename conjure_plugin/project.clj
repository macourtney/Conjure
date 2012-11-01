(defproject org.conjure/conjure-plugin "1.0.0"
  :description "Conjure view is a library to render html for conjure."

  :dependencies [[clojure-tools "1.1.2"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.conjure/conjure-config "1.0.0"]]

  :profiles { :dev { :dependencies [[org.conjure/conjure-flow "1.0.0"]
                                    [org.drift-db/drift-db-h2 "1.1.4"]] } })