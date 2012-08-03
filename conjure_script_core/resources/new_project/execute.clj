(ns execute
  (:require [conjure.execute :as core-execute]))

(apply core-execute/-main *command-line-args*)