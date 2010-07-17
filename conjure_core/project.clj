(defproject conjure-core "0.7.0"
  :description "Core libraries for Conjure. Conjure is a full stack web framework written entirely in Clojure."
  :dependencies [[clout "0.2.0"]
                 [commons-lang/commons-lang "2.5"]
                 [com.h2database/h2 "1.2.137"]
                 [hiccup "0.2.6"]
                 [log4j/log4j "1.2.16"]
                 [org.clojars.macourtney/clj-record "1.0.1"]
                 [org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
                 [ring/ring-core "0.2.5"]
                 [ring/ring-devel "0.2.5"]
                 [ring/ring-jetty-adapter "0.2.5"]
                 [ring/ring-servlet "0.2.5"]
                 [scriptjure "0.1.3"]]
  :dev-dependencies [[lein-clojars "0.5.0"]]
  :aot [conjure.core.execute])