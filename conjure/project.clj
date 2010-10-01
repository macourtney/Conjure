(defproject conjure "0.8.0-RC2"
  :description "Self extracting jar file for Conjure. Conjure is a full stack web framework written entirely in Clojure."

  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [conjure-script "0.8.0-RC2"]]

  :dev-dependencies [[lein-clojars "0.5.0"]]

  :hooks [leiningen.hooks.copy-resource-deps]

  :main conjure.extract)