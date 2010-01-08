(ns environments.test)

; Sets up the logger for test mode.
(logging-utils/load-configuration-map {
  "handlers" "java.util.logging.ConsoleHandler, java.util.logging.FileHandler",
  ".level" "ALL",
  "java.util.logging.ConsoleHandler.level" "FINE",
  "java.util.logging.ConsoleHandler.formatter" "java.util.logging.SimpleFormatter",
  
  "java.util.logging.FileHandler.level" "ALL"
  "java.util.logging.FileHandler.formatter" "java.util.logging.SimpleFormatter",
  "java.util.logging.FileHandler.pattern" "log/test.log"})