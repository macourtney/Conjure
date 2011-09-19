(defproject conjure-core "0.8.4"
  :description "Core libraries for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[clojure-tools "1.0.1"]
                 [clout "0.2.0"]
                 [commons-lang/commons-lang "2.5"]
                 [com.h2database/h2 "1.2.137"]
                 [drift "1.2.2"]
                 [hiccup "0.3.0"]
                 [log4j/log4j "1.2.16"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [org.clojars.macourtney/clj-record "1.0.1"]
                 [ring/ring-core "0.3.0"]
                 [ring/ring-devel "0.3.0"]
                 [ring/ring-jetty-adapter "0.3.0"]
                 [ring/ring-servlet "0.3.0"]
                 [scriptjure "0.1.13"]]
  :dev-dependencies [[lein-clojars "0.6.0"]]
  :aot [conjure.core.execute
        conjure.core.server.servlet]

  :repositories { "maven-gae-plugin-repo" "http://maven-gae-plugin.googlecode.com/svn/repository" })