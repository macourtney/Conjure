(ns conjure.controller.builder
  (:import [java.io File])
  (:require [conjure.controller.util :as util]))

(defn
#^{:doc "Creates a new controller file from the given controller name."}
  create-controller-file 
  [ { :keys [controller controllers-directory silent] 
      :or { controllers-directory (util/find-controllers-directory), silent false } }]
    (if (and controllers-directory controller)
      (let [controller-file (new File controllers-directory (util/controller-file-name-string controller))]
        (if (. controller-file exists)
          (if (not silent) (println (. controller-file getName) "already exists. Doing nothing."))
          (do
            (if (not silent) (println "Creating controller file" (. controller-file getName) "..."))
            (. controller-file createNewFile)
            controller-file)))))