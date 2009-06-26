(ns conjure.controller.builder
  (:import [java.io File])
  (:require [conjure.controller.util :as util]))

(defn
#^{:doc "Creates a new controller file from the given controller name."}
  create-controller-file [controllers-directory controller-name]
  (let [controller-file (new File controllers-directory (util/controller-file-name-string controller-name))]
    (if (. controller-file exists)
      (println (. controller-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating controller file" (. controller-file getName) "...")
        (. controller-file createNewFile)
        controller-file))))