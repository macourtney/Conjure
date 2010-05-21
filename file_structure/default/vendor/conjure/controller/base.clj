(ns conjure.controller.base
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [conjure.controller.util :as controller-util]
            [conjure.binding.util :as bind-util]
            [conjure.view.util :as view-util]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.string-utils :as string-utils]))

(defn
#^{ :doc "Runs the binding associated with the given controller and action passing it the given params." }
  bind-by-controller-action [controller action params]
  (bind-util/call-binding controller action params))

(defn 
#^{ :doc "Runs the binding associated with the controller and action in the given request-map." }
  bind [{ :keys [controller action], :as request-map } & params]
  (bind-by-controller-action controller action (cons request-map params)))

(defn
#^{ :doc "Redirects to the given url with the given status. If status is not given, 302 (redirect found) is used." }
  redirect-to-full-url
  ([url] (redirect-to-full-url url 302))
  ([url status] 
    { :status status
      :headers 
        { "Location" url
          "Connection" "close" }
      :body (str "<html><body>You are being redirected to <a href=\"" url "\">" url "</a></body></html>") })) ; 

(defn-
#^{ :doc "Determines the type of redirect-to called. Possible values: :string, :request-map, :parameters. Uses 
render-type? to determine :request-map or :parameters." }
  redirect-type? [& params]
  (if (or (string? (first params)) (string? (second params)))
    :string
    :request-map))

(defmulti
#^{ :doc "Redirects to either the given url or a url generated from the given parameters and request map." }
  redirect-to redirect-type?)

(defmethod redirect-to :string 
  ([url] (redirect-to-full-url url))
  ([request-map url]
    (redirect-to-full-url
      (html-utils/full-url url (view-util/full-host request-map)))))
    
(defmethod redirect-to :request-map 
  ([request-map] (redirect-to-full-url (view-util/url-for request-map)))
  ([request-map params]
    (let [status (:status params)]
      (if status
        (redirect-to-full-url (view-util/url-for request-map (dissoc params :status)) status)
        (redirect-to-full-url (view-util/url-for request-map params))))))

(defn
#^{ :doc "Adds the given action function to the list of action functions to call." }
  add-action-function [action-function params]
  (controller-util/add-action-function action-function params))

(defn
#^{ :doc "Returns the controller from the given controller namespace." }
  controller-from-namespace [namespace]
  (controller-util/controller-from-namespace (name (ns-name namespace))))

(defmacro defaction [action-name & body]
  (let [attributes (first body)
        controller (controller-from-namespace *ns*)
        params { :action (str action-name), :controller controller }]
    (if (map? attributes)
      (let [new-params (merge params attributes)]
        `(add-action-function 
          (fn [~'request-map] ~@(rest body)) 
          ~new-params))
      `(add-action-function 
        (fn [~'request-map] ~@body) 
        ~params))))

(defn
#^{ :doc "Returns the name of the interceptor based on the given interceptor symbol." }
  interceptor-name [interceptor-symbol]
  (name interceptor-symbol))

(defmacro add-interceptor
  ([interceptor] 
    (let [controller (controller-from-namespace *ns*)
          interceptor-name (interceptor-name interceptor)]
      `(controller-util/add-interceptor ~interceptor ~interceptor-name ~controller nil nil))) 
  ([interceptor params]
    (let [controller (controller-from-namespace *ns*)
          interceptor-name (interceptor-name interceptor)
          excludes (:excludes params)
          includes (:includes params)]
      `(controller-util/add-interceptor ~interceptor ~interceptor-name ~controller ~excludes ~includes))))

(defn 
#^{ :doc "Adds the given interceptor as an app interceptor. The interceptor will be run for every controller and action
unless it is explicitly excluded in the given params." }
  add-app-interceptor 
  ([interceptor] (add-app-interceptor interceptor {}))
  ([interceptor { :keys [excludes] :or { excludes {} }}]
    (controller-util/add-app-interceptor interceptor excludes)))

(defmacro
#^{ :doc "Copies the actions from the given controller into this one. If a filter map is given, then the actions from 
the from controller are filtered based on the includes and excludes keys of the filter map. Includes and excludes must
be sets of action name keywords." } 
  copy-actions 
  ([from-controller]
    (let [to-controller (controller-from-namespace *ns*)]
      `(controller-util/copy-actions ~to-controller ~from-controller)))
  ([from-controller filter-map]
    (let [to-controller (controller-from-namespace *ns*)]
      `(controller-util/copy-actions ~to-controller ~from-controller ~filter-map))))