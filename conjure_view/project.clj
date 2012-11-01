(defproject org.conjure/conjure-view "1.0.0"
  :description "Conjure view is a library to render html for conjure."
  :dependencies [[clojure-tools "1.1.2"]
                 [org.conjure/conjure-config "1.0.0"]
                 [org.conjure/conjure-html "1.0.0"]
                 [org.conjure/conjure-util "1.0.0"]
                 [scriptjure "0.1.24"]]

  :profiles { :dev { :dependencies [[org.conjure/conjure-model "1.0.0"]
                                    [org.drift-db/drift-db "1.1.4"]
                                    [org.drift-db/drift-db-h2 "1.1.4"]] } })