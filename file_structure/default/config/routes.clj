;; This file is used to route requests to the appropriate controller and action.

(ns routes
  (:use [conjure.util string-utils]))

(defn draw []
  [(fn [path] 
     (let [path_tokens (tokenize path "/")
           controller (nth path_tokens 0 nil)
           action (nth path_tokens 1 nil)
           id (nth path_tokens 2 nil)]

       (if (and controller action)
         {:controller controller
         :action action
         :id id}
         nil)))])