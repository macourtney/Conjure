(defproject org.conjure/conjure-flow "1.0.0"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-config "1.0.0"]
                 [org.conjure/conjure-util "1.0.0"]]

  :profiles { :dev { :dependencies [[log4j/log4j "1.2.17"]
                                    [org.conjure/conjure-model "1.0.0"]
                                    [org.conjure/conjure-view "1.0.0"]
                                    [org.drift-db/drift-db "1.1.4"]
                                    [org.drift-db/drift-db-h2 "1.1.4"]] } })