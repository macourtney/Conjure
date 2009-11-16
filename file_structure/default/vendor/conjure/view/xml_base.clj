(ns conjure.view.xml-base)

(defn
#^{ :doc "Creates an xml response using the given body. Body should be a string containing the xml contents." }
  xml-response [body]
  { :status  200
    :headers {"Content-Type" "text/xml"}
    :body body })

(defmacro
#^{:doc "Defines an xml view. This macro should be used in an xml view file to define the parameters used in the view."}
  defxml [params & body]
  `(defn ~'render-view [~'request-map ~@params]
    (xml-response ~@body)))