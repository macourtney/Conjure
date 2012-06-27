(ns conjure.execute
  (:require [conjure.util.execute-utils :as execute-utils]) 
  (:gen-class))

(defn -main [& args]
  (execute-utils/run-args args))