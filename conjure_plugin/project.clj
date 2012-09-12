(defproject org.conjure/conjure-plugin "0.9.0-SNAPSHOT"
  :description "Conjure view is a library to render html for conjure."

  :dependencies [[clojure-tools "1.1.2-SNAPSHOT"]
                 [org.clojure/tools.logging "0.2.0"]
                 [org.conjure/conjure-config "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[org.conjure/conjure-server "0.9.0-SNAPSHOT"]
                                    [org.conjure/conjure-scaffold "0.9.0-SNAPSHOT"] 
                                    [org.drift-db/drift-db-h2 "1.1.2"]] } })