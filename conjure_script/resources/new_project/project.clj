(defproject my-project "1.0.0-SNAPSHOT"
  :description "This is a default Conjure Leiningen project."
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [conjure-core "0.7.0-SNAPSHOT"]]
  :dev-dependencies [[lein-conjure "0.7.0-SNAPSHOT"]]
  
  :main run-server)