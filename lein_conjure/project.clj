(defproject org.conjure/lein-conjure "1.0.0"
  :description "A leiningen plugin for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[org.conjure/conjure-script-core "1.0.0"]
                 [org.conjure/conjure-script-plugin "1.0.0"]
                 [org.conjure/conjure-script-scaffold "1.0.0"]
                 [org.conjure/conjure-util "1.0.0"]]

  :profiles { :dev { :dependencies [[org.drift-db/drift-db-h2 "1.1.4"]] } }

  :eval-in-leiningen true)