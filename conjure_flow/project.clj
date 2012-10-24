(defproject org.conjure/conjure-flow "0.9.0-SNAPSHOT"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.clojure/clojure "1.4.0"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                                    [org.conjure/conjure-scaffold "0.9.0-SNAPSHOT"] 
                                    [org.conjure/conjure-view "0.9.0-SNAPSHOT"]
                                    [org.drift-db/drift-db "1.1.3"]
                                    [org.drift-db/drift-db-h2 "1.1.3"]] } })