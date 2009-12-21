(ns conjure.server.server
  (:import [java.util Calendar Date])
  (:require [http-config :as http-config]
            [environment :as environment]
            [routes :as routes]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.model.database :as database]
            [conjure.controller.util :as controller-util]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.view.util :as view-util]
            [conjure.util.html-utils :as html-utils]
            [clojure.contrib.str-utils :as str-utils]))

(defn
#^{:doc "Merges the params value of the given request-map with params"}
  augment-params [request-map params]
  (if request-map
    (let [output-params (request-map :params)]
      (if (and output-params params (not-empty params))
        (assoc request-map :params (merge output-params params))
        request-map))))

(defn
#^{:doc "Returns a parameter map generated from the post content."}
  parse-post-params [request-map]
  (if (= (:request-method request-map) :post)
    (html-utils/parse-query-params 
      (loading-utils/string-input-stream (:body request-map) (:content-length request-map)))
    {}))

(defn
#^{:doc "Parses all of the params from the given request map."}
  parse-params [request-map]
  (merge (parse-post-params request-map) (html-utils/parse-query-params (:query-string request-map))))

(defn
#^{:doc "Gets a route map for use by conjure to call the correct methods."}
  update-request-map [request-map]
  (let [path (:uri request-map)
        params (parse-params request-map)
        output (augment-params (some identity (map #(% path) (routes/draw))) params)]
    (if output
      (merge request-map output)
      (assoc request-map :params params )))) 

(defn
#^{:doc "Returns the controller file name generated from the given request map."}
  controller-file-name [request-map]
  (controller-util/controller-file-name-string (:controller request-map)))
  
(defn
#^{:doc "Returns fully qualified action generated from the given request map."}
  fully-qualified-action [request-map]
  (if request-map
    (let [controller (:controller request-map)
          action (:action request-map)]
      (if (and controller action)
        (str (controller-util/controller-namespace controller) "/" action)))))

(defn
#^{:doc "Loads the given controller file."}
  load-controller [controller-filename]
  (loading-utils/load-resource "controllers" controller-filename))

(defn
#^{ :doc "Initializes the conjure server." }
  init []
  (database/ensure-conjure-db))

(defn
#^{ :doc "Updates the response map with a session cookie if necessary." }
  manage-session [request-map response-map]
  (if (not (or (get (:headers request-map) "cookie") (get (:headers response-map) "Set-Cookie")))
    (let [tomorrow (doto (. Calendar getInstance)
     								 (.add (. Calendar DATE) 1))]
	    (assoc
	      response-map 
	      :headers 
	        (merge 
	          (:headers response-map)
		      	{ "Set-Cookie"
		       		(str 
		       		  "SID=" 
			      		(conjure-str-utils/md5-sum 
			      			"Conjure" 
			      			(str (. (new Date) getTime)) 
			      	  	(str (. Math random)))
			      	 "; expires=" 
			      	 (html-utils/format-cookie-date (. tomorrow getTime))
			      	 "; path=/") })))
    response-map))

(defn
#^{ :doc "Converts the given response to a response map if it is not already 
one." }
  create-response-map [response]
  (if (map? response)
    response
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    response}))

(defn
#^{ :doc "Takes the given path and calls the correct controller and action for it." }
  process-request [request-map]
  (when request-map
    (init)
    (let [generated-request-map (update-request-map request-map)
          controller-file (controller-file-name generated-request-map)]
      (if controller-file
        (do
          (load-controller controller-file)
          (manage-session 
          	request-map
          	(create-response-map 
          		((load-string (fully-qualified-action generated-request-map)) 
          			generated-request-map))))
        nil))))

(defn
#^{ :doc "A function for simplifying the loading of views." }
  render-view [request-map & params]
  (view-util/load-view request-map)
  (apply (read-string (fully-qualified-action request-map)) request-map params))

(defn
#^{ :doc "Gets the user configured http properties." }
  http-config []
  (http-config/get-http-config))

(defn
#^{:doc "Gets the user configured database properties."}
  db-config []
  database/conjure-db)