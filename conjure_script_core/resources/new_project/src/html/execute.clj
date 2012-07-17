(ns execute
  (:require [conjure.core.execute :as core-execute]))

(apply core-execute/-main *command-line-args*)