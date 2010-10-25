(ns config.environments.production
  (:import [org.apache.log4j ConsoleAppender FileAppender Level Logger PatternLayout]
           [org.apache.log4j.varia LevelRangeFilter]))

(def use-logger? (or (:use-logger? environment/properties) true))

(when use-logger?

  ; Sets up the logger for production mode.
  (def output-pattern (new PatternLayout "%-5p [%c]: %m%n")))

  (def file-appender (new FileAppender output-pattern "log/production.log"))
  (.addFilter file-appender 
    (doto (new LevelRangeFilter)
      (.setLevelMin (. Level ALL))))

  (def console-appender (new ConsoleAppender output-pattern))
  (.addFilter console-appender 
    (doto (new LevelRangeFilter)
      (.setLevelMin (. Level ERROR))))

  (doto (. Logger getRootLogger)
    (.setLevel (. Level ALL))
    (.addAppender file-appender)
    (.addAppender console-appender))