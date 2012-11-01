(defproject org.conjure/conjure-view "0.9.0-SNAPSHOT"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-html "0.9.0"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]
                 [scriptjure "0.1.24"]]

  :profiles { :dev { :dependencies [[org.conjure/conjure-model "0.9.0-SNAPSHOT"]
                                    [org.drift-db/drift-db "1.1.3"]
                                    [org.drift-db/drift-db-h2 "1.1.3"]] } })