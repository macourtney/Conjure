(ns config.environment
  (:require [clojure.contrib.java-utils :as java-utils]))

(def properties
  (atom 
    { :assets-dir "public"
      :javascripts-dir "javascripts"
      :stylesheets-dir "stylesheets"
      :images-dir "images"
      
      :jquery "jquery-1.3.2.min.js"
      :conjure-js "conjure.js"

      :use-logger? true }))