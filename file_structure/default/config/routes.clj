;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:require [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.str-utils :as contrib-str-utils]))

(defn draw []
  [(fn [path]
     (if path
       (let [path-tokens (contrib-str-utils/re-split #"/" path)
             controller (nth path-tokens 1 nil)
             action (nth path-tokens 2 nil)
             id (nth path-tokens 3 nil)]

         (if (and controller action)
           { :controller (loading-utils/underscores-to-dashes controller)
             :action (loading-utils/underscores-to-dashes action)
             :params (if id {:id id} {}) }
           nil))
       nil))])