(ns conjure.flow.builder
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [conjure.flow.util :as util]))

(defn
#^{:doc "Creates a new flow file from the given service."}
  create-flow-file 
  [ { :keys [service flows-directory silent] 
      :or { flows-directory (util/find-flows-directory), silent false } }]
    (if (and flows-directory service)
      (let [flow-file (new File flows-directory (util/flow-file-name-string service))]
        (if (.exists flow-file)
          (logging/info (str (.getName flow-file) " already exists. Doing nothing."))
          (do
            (logging/info (str "Creating flow file " (.getName flow-file) "..."))
            (.createNewFile flow-file)
            flow-file)))))