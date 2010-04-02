(ns conjure.binding.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.binding.util :as binding-util]
            [conjure.util.loading-utils :as loading-utils]))

(defn 
#^{:doc "Finds or creates if missing, a controller directory for the given controller in the given bindings directory."}
  find-or-create-controller-directory
  ([{ :keys [bindings-directory controller silent] 
      :or { bindings-directory (binding-util/find-bindings-directory), silent false } }]
    (if (and bindings-directory controller)
      (let [controller-directory (binding-util/find-controllers-directory bindings-directory controller)]
        (if (and controller-directory (.exists controller-directory))
          (do
            (logging/info (str (. controller-directory getPath) " directory already exists."))
            controller-directory)
          (do
            (logging/info "Creating controller directory in views...")
            (let [new-controller-directory (new File bindings-directory (loading-utils/dashes-to-underscores controller))]
              (. new-controller-directory mkdirs)
              new-controller-directory)))))))

(defn
#^{:doc "Creates a new binding file from the given controller and action name."}
  create-binding-file 
  [ { :keys [controller action controllers-directory silent] 
      :or { controllers-directory nil, silent false } }]
    (if (and controller action)
      (let [found-controllers-directory (or controllers-directory (binding-util/find-controllers-directory controller))
            binding-file (new File found-controllers-directory (binding-util/binding-file-name-string action))]
        (if (. binding-file exists)
          (logging/info (str (. binding-file getName) " already exists. Doing nothing."))
          (do
            (logging/info (str "Creating binding file " (. binding-file getName) "..."))
            (. binding-file createNewFile)
            binding-file)))))