(ns config.environment)

(def properties
  (atom 
    { :default-environment "test"

      :source-dir "test"

      :call-controller-fn #(identity true) }))
