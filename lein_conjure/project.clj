(defproject lein-conjure "0.7.0-SNAPSHOT"
  :description "A leiningen plugin for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [conjure-script "0.7.0-SNAPSHOT"]
                 [ant/ant "1.6.5"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]]
  :dev-dependencies [[lein-clojars "0.5.0"]])