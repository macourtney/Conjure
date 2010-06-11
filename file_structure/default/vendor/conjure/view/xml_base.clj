(ns conjure.view.xml-base
  (:import [java.io StringWriter])
  (:require [clojure.contrib.prxml :as prxml]))

(defn
#^{ :doc "Creates an xml response using the given body. Body should be a string containing the xml contents." }
  xml-response []
  { :status  200
    :headers {"Content-Type" "text/xml"} })

(defmacro
#^{ :doc "Defines a view. This macro should be used in a view file to define the parameters used in the view." }
  def-xml [params & body]
  (let [def-params (if (map? params) params {})
        view-params (if (map? params) (first body)  params)
        response-map (or (:response-map def-params) (xml-response))]
   `(do
      (defn ~'render-body [~@view-params]
        ~@body)
      (defn ~'render-str [~@view-params]
        (with-open [string-writer# (new StringWriter)] 
          (binding [*out* string-writer#]
            (prxml/prxml
              (~'render-body ~@view-params)))
          (.toString string-writer#)))
      (defn ~'render-view [~@view-params]
        (assoc ~response-map :body (~'render-str ~@view-params))))))