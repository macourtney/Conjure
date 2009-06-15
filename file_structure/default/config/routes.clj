;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:require [conjure.util.string-utils :as string-utils]))

(defn draw []
  [(fn [path] 
     (let [path_tokens (string-utils/tokenize path "/")
           controller (first path_tokens)
           path_tokens_2 (rest path_tokens)
           action (first path_tokens_2)
           path_tokens_3 (rest path_tokens_2)
           id (first path_tokens_3)]

       (if (and controller action)
         {:controller controller
         :action action
         :params {:id id}}
         nil)))])