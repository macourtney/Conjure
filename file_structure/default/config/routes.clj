;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:require [conjure.util.string-utils :as string-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn draw []
  [(fn [path]
     (if path
       (let [path_tokens (string-utils/tokenize path "/")
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