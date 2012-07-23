(ns conjure.script.view.util
  (:require [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.logging :as logging]
            [conjure.test.util :as test-util]
            [conjure.view.util :as util]))

(defn usage
  "Prints the usage text based on the given script to run and the file type to run it on."
  [script file-type]
  (println "You must supply a service and action name (Like hello-world).")
  (println (str "Usage: ./run.sh script/" script ".clj") file-type "<service> <action>"))

(defn destroy-usage
  "Prints the destroy usage text based on the given file type."
  [file-type]
  (usage "destroy" file-type))

(defn generate-usage
  "Prints the generate usage text based on the given file type."
  [file-type]
  (usage "generate" file-type))

(defn find-views-directory
  "Returns the views directory or logs an error if the views directory could not be found."
  []
  (if-let [view-directory (util/find-views-directory)]
    view-directory
    (do
      (logging/error (str "Could not find views directory."))
      (logging/error "Command ignored."))))

(defn find-service-directory
  "Returns the view service directory for the given service. If the service cannot be found, this function logs an error
and returns nil."
  ([service] (find-service-directory service (find-views-directory)))
  ([service view-directory]
    (when view-directory
      (if-let [service-directory (util/find-service-directory view-directory service)]
        service-directory
        (logging/error (str "The view directory for service " service " was not found. Doing nothing."))))))

(defn find-view-file
  ([service action] (find-view-file service action (find-service-directory service)))
  ([service action service-directory]
    (when service-directory
      (if-let [view-file (util/find-view-file service-directory action)]
        view-file
        (logging/info "View file not found. Doing nothing.")))))

(defn find-service-view-unit-test-directory
  [service]
  (if-let [service-view-unit-test-dir (test-util/find-service-view-unit-test-directory service)]
    service-view-unit-test-dir
    (do
      (logging/error (str "Could not find the " (loading-utils/dashes-to-underscores service) " test directory."))
      (logging/error "Command ignored."))))

(defn find-view-unit-test-file
  ([service action] (find-view-unit-test-file service action (find-service-view-unit-test-directory service)))
  ([service action service-view-unit-test-dir]
    (when service-view-unit-test-dir
      (if-let [view-unit-test-file (test-util/view-unit-test-file service action service-view-unit-test-dir)]
        view-unit-test-file
        (logging/info "View test file not found. Doing nothing.")))))