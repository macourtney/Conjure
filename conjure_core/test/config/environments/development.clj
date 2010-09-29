(ns config.environments.development
  (:import [org.apache.log4j ConsoleAppender FileAppender Level Logger PatternLayout]
           [org.apache.log4j.varia LevelRangeFilter])
  (:require [clojure.tools.logging-utils :as logging-utils]
            [clojure.contrib.logging :as logging]))

; Sets up the logger for development mode.
(def output-pattern (new PatternLayout "%-5p [%c]: %m%n"))

(def file-appender (new FileAppender output-pattern "log/development.log"))
(.addFilter file-appender 
  (doto (new LevelRangeFilter)
    (.setLevelMin (. Level ALL))))
    
(def console-appender (new ConsoleAppender output-pattern))
(.addFilter console-appender 
  (doto (new LevelRangeFilter)
    (.setLevelMin (. Level ALL))))

(doto (. Logger getRootLogger)
  (.setLevel (. Level ALL))
  (.addAppender file-appender)
  (.addAppender console-appender))

(in-ns 'environment)

(def reload-files true)