(defproject conjure-core "0.8.7-SNAPSHOT"
  :description "Core libraries for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[clj-record "1.1.0"]
                 [clojure-tools "1.1.1-SNAPSHOT"]
                 [clout "1.0.0"]
                 [org.apache.commons/commons-lang3 "3.1"]
                 [drift "1.4.3-SNAPSHOT"]
                 [log4j/log4j "1.2.16"]
                 [org.clojure/clojure "1.2.1"]
                 [org.clojure/data.xml "0.0.1-SNAPSHOT"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.conjure/conjure-html "0.9.0-SNAPSHOT"]
                 [org.drift-db/drift-db "1.0.7-SNAPSHOT"]
                 [ring/ring-core "1.0.1"]
                 [ring/ring-devel "1.0.1"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [ring/ring-servlet "1.0.1"]
                 [scriptjure "0.1.24"]]
  :dev-dependencies [[org.drift-db/drift-db-h2 "1.0.7-SNAPSHOT"]
                     [org.clojure/clojure "1.2.1"]]
  :aot [conjure.core.execute
        conjure.core.server.servlet])