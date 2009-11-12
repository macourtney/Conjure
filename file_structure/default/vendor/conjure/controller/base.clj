(ns conjure.controller.base
  (:require [conjure.controller.util :as controller-util]
            [conjure.view.util :as view-util]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.string-utils :as string-utils]))

(defn
#^{:doc "Determines the type of render-view called. Possible values: :request-map, :parameters."}
  render-type? [request-map & params]
  (if (and (contains? request-map :controller) (contains? request-map :action))
    :request-map
    :parameters))

(defmulti render-view "Renders the view given in the request-map." render-type?)

(defmethod render-view :request-map [request-map & params]
  (apply render-view { :layout "application" } request-map params))

(defmethod render-view :parameters [parameters request-map & params]
  (view-util/render-layout (:layout parameters) request-map
    (apply view-util/render-view request-map params)))

(defn
#^{:doc "Redirects to the given url with the given status. If status is not given, 302 (redirect found) is used."}
  redirect-to-full-url
  ([url] (redirect-to-full-url url 302))
  ([url status] 
    { :status status
      :headers 
        { "Location" url
          "Connection" "close" }
      :body (str "<html><body>You are being redirected to <a href=\"" url "\">" url "</a></body></html>") })) ; 

(defn-
#^{:doc "Determines the type of redirect-to called. Possible values: :string, :request-map, :parameters. Uses 
render-type? to determine :request-map or :parameters."}
  redirect-type? [& params]
  (if (or (string? (first params)) (string? (second params)))
    :string
    :request-map))

(defmulti
#^{:doc "Redirects to either the given url or a url generated from the given parameters and request map."}
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