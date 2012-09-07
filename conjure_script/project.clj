(defproject conjure-script "0.8.9-SNAPSHOT"
  :description "Script libraries for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[conjure-core "0.8.9-SNAPSHOT"]
                 [org.clojure/tools.cli "0.2.1"]]

  :profiles { :dev { :dependencies [[org.drift-db/drift-db-h2 "1.1.2"]] } })