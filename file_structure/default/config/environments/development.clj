(ns environments.development
  (:require [conjure.util.logging-utils :as logging-utils]))

; Sets up the logger for development mode.
(logging-utils/load-configuration-map {
  "handlers" "java.util.logging.ConsoleHandler, java.util.logging.FileHandler",
  ".level" "ALL",
  "java.util.logging.ConsoleHandler.level" "FINE",
  "java.util.logging.ConsoleHandler.formatter" "java.util.logging.SimpleFormatter",
  
  "java.util.logging.FileHandler.level" "ALL"
  "java.util.logging.FileHandler.formatter" "java.util.logging.SimpleFormatter",
  "java.util.logging.FileHandler.pattern" "log/development.log"})