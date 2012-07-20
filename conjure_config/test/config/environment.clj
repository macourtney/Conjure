(ns config.environment)

(def properties
  (atom 
    { :default-environment "test"

      :source-dir "test"

      :call-service-fn #(identity true) }))
