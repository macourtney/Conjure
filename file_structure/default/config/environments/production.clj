(ns environments.production
  (:require [conjure.util.logging-utils :as logging-utils]))

; Sets up the logger for production mode.
(logging-utils/load-configuration-map {
  "handlers" "java.util.logging.ConsoleHandler, java.util.logging.FileHandler",
  ".level" "ALL",
  "java.util.logging.ConsoleHandler.level" "INFO",
  "java.util.logging.ConsoleHandler.formatter" "java.util.logging.SimpleFormatter",
  
  "java.util.logging.FileHandler.level" "ALL"
  "java.util.logging.FileHandler.formatter" "java.util.logging.SimpleFormatter",
  "java.util.logging.FileHandler.pattern" "log/production.log"})