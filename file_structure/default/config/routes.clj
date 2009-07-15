;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:require [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.str-utils :as contrib-str-utils]))

(defn draw []
  [(fn [path]
     (if path
       (let [path_tokens (contrib-str-utils/re-split #"/" path)
             controller (first path_tokens)
             path_tokens_2 (rest path_tokens)
             action (first path_tokens_2)
             path_tokens_3 (rest path_tokens_2)
             id (first path_tokens_3)]

         (if (and controller action)
           { :controller (loading-utils/underscores-to-dashes controller)
             :action (loading-utils/underscores-to-dashes action)
             :params (if id {:id id} {}) }
           nil))
       nil))])