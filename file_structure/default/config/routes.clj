;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:use [conjure.util string-utils]))

(defn draw []
  [(fn [path] 
     (let [path_tokens (tokenize path "/")
           controller (first path_tokens)
           path_tokens_2 (rest path_tokens)
           action (first path_tokens_2)
           path_tokens_3 (rest path_tokens_2)
           id (first path_tokens_3)]

       (if (and controller action)
         {:controller controller
         :action action
         :id id}
         nil)))])