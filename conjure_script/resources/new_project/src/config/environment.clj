(ns config.environment
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.core.util.loading-utils :as loading-utils]))

(def properties
  (atom 
    { :assets-dir "public"
      :javascripts-dir "javascripts"
      :stylesheets-dir "stylesheets"
      :images-dir "images"
      
      :jquery "jquery-1.3.2.min.js"
      :conjure-js "conjure.js" }))