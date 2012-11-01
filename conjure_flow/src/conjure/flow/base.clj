(ns conjure.flow.base
  (:require [conjure.flow.util :as flow-util]
            [conjure.config.environment :as environment]
            [conjure.util.conjure-utils :as conjure-utils]))


(defn render
  "Renders the view for the current service and action passing the given params along to the view."
  [& params]
  (apply environment/render-service params))

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
#^{ :doc "Determines the type of redirect-to called. Possible values: :string, :params." }
  redirect-type? [& params]
  (if (or (string? (first params)))
    :string
    :params))

(defmulti
#^{ :doc "Redirects to either the given url or a url generated from the given parameters and request map." }
  redirect-to redirect-type?)

(defmethod redirect-to :string 
  [url] (redirect-to-full-url url))
    
(defmethod redirect-to :params
  [params]
    (if-let [status (:status params)]
      (redirect-to-full-url (conjure-utils/url-for (dissoc params :status)) status)
      (redirect-to-full-url (conjure-utils/url-for params))))

(defn
#^{ :doc "A short cut function to simply redirect to another action." }
  redirect-to-action
  ([action] (redirect-to { :action action }))
  ([action params] (redirect-to { :action action :params params })))

(defn
#^{ :doc "Adds the given action function to the list of action functions to call." }
  add-action-function [action-function params]
  (flow-util/add-action-function action-function params))

(defn
#^{ :doc "Returns the service from the given flow namespace." }
  service-from-namespace [namespace]
  (flow-util/service-from-namespace (name (ns-name namespace))))

(defmacro def-action [action-name & body]
  (let [attributes (first body)
        service (service-from-namespace *ns*)
        params { :action (str action-name), :service service }]
    (if (map? attributes)
      (let [new-params (merge params attributes)]
        `(add-action-function 
          (fn [] ~@(rest body)) 
          ~new-params))
      `(add-action-function 
        (fn [] ~@body) 
        ~params))))

(defn
#^{ :doc "Returns the name of the interceptor based on the given interceptor symbol." }
  interceptor-name-from [interceptor-symbol]
  (name interceptor-symbol))

(defmacro add-interceptor
  ([interceptor] 
    (let [service (service-from-namespace *ns*)
          interceptor-name (interceptor-name-from interceptor)]
      `(flow-util/add-interceptor ~interceptor ~interceptor-name ~service nil nil))) 
  ([interceptor { :keys [includes excludes interceptor-name] }]
    (let [service (service-from-namespace *ns*)
          interceptor-name (or interceptor-name (interceptor-name-from interceptor))]
      `(flow-util/add-interceptor ~interceptor ~interceptor-name ~service ~excludes ~includes))))

(defn 
#^{ :doc "Adds the given interceptor as an app interceptor. The interceptor will be run for every service and action
unless it is explicitly excluded in the given params." }
  add-app-interceptor 
  ([interceptor] (add-app-interceptor interceptor {}))
  ([interceptor { :keys [excludes] :or { excludes {} }}]
    (flow-util/add-app-interceptor interceptor excludes)))

(defmacro
#^{ :doc "Copies the actions from the given service into this one. If a filter map is given, then the actions from 
the from service are filtered based on the includes and excludes keys of the filter map. Includes and excludes must
be sets of action name keywords." } 
  copy-actions 
  ([from-service]
    (let [to-service (service-from-namespace *ns*)]
      `(flow-util/copy-actions ~to-service ~from-service)))
  ([from-service filter-map]
    (let [to-service (service-from-namespace *ns*)]
      `(flow-util/copy-actions ~to-service ~from-service ~filter-map))))

(defn create-render-only-actions
  "Creates render only actions for the service and given actions."
  [service & actions]
  (doseq [action actions]
    (add-action-function render { :action (name action), :service service })))

(defmacro def-render-only-actions
  "Creates a render only action for each of the given actions for the service generated from the current namespace."
  [& actions]
  (let [service (service-from-namespace *ns*)]
    `(create-render-only-actions ~service ~@actions)))