(ns conjure.flow.builder
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [conjure.flow.util :as util]))

(defn
#^{:doc "Creates a new controller file from the given controller name."}
  create-controller-file 
  [ { :keys [controller controllers-directory silent] 
      :or { controllers-directory (util/find-controllers-directory), silent false } }]
    (if (and controllers-directory controller)
      (let [controller-file (new File controllers-directory (util/controller-file-name-string controller))]
        (if (.exists controller-file)
          (logging/info (str (.getName controller-file) " already exists. Doing nothing."))
          (do
            (logging/info (str "Creating controller file " (.getName controller-file) "..."))
            (.createNewFile controller-file)
            controller-file)))))