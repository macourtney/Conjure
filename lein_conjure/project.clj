(defproject org.conjure/lein-conjure "0.9.0-SNAPSHOT"
  :description "A leiningen plugin for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[org.conjure/conjure-script-core "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-script-plugin "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-script-scaffold "0.9.0-SNAPSHOT"]
                 [org.conjure/conjure-util "0.9.0-SNAPSHOT"]]

  :profiles { :dev { :dependencies [[org.drift-db/drift-db-h2 "1.1.2"]] } }

  :eval-in-leiningen true)