(ns conjure.script.plugin 
  (:require [conjure.server.server :as server]
            [conjure.plugin.util :as plugin-util]))

(defn print-usage []
  (println "Usage: ./run.sh script/plugin.clj <install|uninstall> <plugin name> <arguments>"))

(defn print-unknown-plugin [plugin-name]
  (println (str "Could not find plugin with name: " plugin-name))
  (print-usage))

(defn print-invalid-plugin [plugin-name]
  (println (str "Invalid plugin: " plugin-name ". The plugin must implement install, uninstall and initialize functions.")))

(defn install [plugin-name arguments]
  (let [install-fn (plugin-util/install-fn plugin-name)]
    (if install-fn
      (install-fn arguments)
      (print-invalid-plugin plugin-name))))

(defn uninstall [plugin-name arguments]
  (let [uninstall-fn (plugin-util/uninstall-fn plugin-name)]
    (if uninstall-fn
      (uninstall-fn arguments)
      (print-invalid-plugin plugin-name))))

(defn test-plugin [plugin-name arguments]
  (plugin-util/run-plugin-tests plugin-name arguments))

(server/init)

(let [type-command (first *command-line-args*)
      plugin-name (second *command-line-args*)
      arguments (drop 2 *command-line-args*)]
  (if plugin-name
    (cond 
      (= type-command "install") (install plugin-name arguments)
      (= type-command "uninstall") (uninstall plugin-name arguments)
      (= type-command "test") (test-plugin plugin-name arguments)
      true (print-usage))
    (print-usage)))