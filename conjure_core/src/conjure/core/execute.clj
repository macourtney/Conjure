(ns conjure.core.execute
  (:require [conjure.core.util.execute-utils :as execute-utils]) 
  (gen-class))

(defn -main [& args]
  (execute-utils/run-args args))