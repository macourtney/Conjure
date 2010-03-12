(ns conjure.controller.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.controller.util :as util]))

(defn
#^{:doc "Creates a new controller file from the given controller name."}
  create-controller-file 
  [ { :keys [controller controllers-directory silent] 
      :or { controllers-directory (util/find-controllers-directory), silent false } }]
    (if (and controllers-directory controller)
      (let [controller-file (new File controllers-directory (util/controller-file-name-string controller))]
        (if (. controller-file exists)
          (logging/info (str (. controller-file getName) " already exists. Doing nothing."))
          (do
            (logging/info (str "Creating controller file " (. controller-file getName) "..."))
            (. controller-file createNewFile)
            controller-file)))))