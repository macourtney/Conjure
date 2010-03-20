(ns conjure.view.link)
  
(in-ns 'conjure.view.base)

(require ['conjure.view.util :as 'view-utils])

(defn
#^{ :doc "Returns the attributes for the link tag (\"a\" tag) from the given request-map." }
  a-attributes [request-map]
  (let [html-options (or (:html-options request-map) {})]
    (if (:href html-options)
      html-options
      (assoc html-options :href (view-utils/url-for request-map)))))

(defn
#^{ :doc 
"Returns a link for the given text and parameters using url-for. Params has the same valid parameters as url-for, plus:

     :html-options - a map of html attributes to add to the anchor tag. If html-options contains a :href key, the value 
                     override the href generated from params.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  link-to
  ([text request-map params] (link-to text (view-utils/merge-url-for-params request-map params)))
  ([text request-map]
    [:a (a-attributes request-map) (evaluate-if-fn text request-map)]))

(defn
#^{:doc "If condition is true, then call link-to with the given text, request-map and params. If condition is false, 
then just return text. If condition is a function, it is evaluated with params merged with request-map. If text is a 
function, it is evaluated with params merged with request-map (just like link-to)." }
  link-to-if
  ([condition text request-map params] (link-to-if condition text (view-utils/merge-url-for-params request-map params)))
  ([condition text request-map]
    (if (evaluate-if-fn condition request-map)
      (link-to text request-map)
      (evaluate-if-fn text request-map))))

(defn-
#^{:doc "Inverses the results of condition. If condition is a function, then this method creates a new function which 
wraps condition, forwarding any parameters to it, but inversing the result." }
  inverse-condition [condition]
  (if (fn? condition)
    (fn [& args] (not (apply condition args)))
    (not condition)))

(defn
#^{:doc "Simply calls link-to-if with the inverse of condition. If condition is a function then a new function is 
created to wrap it, and simply inverse the result of condition." }
  link-to-unless
    ([condition text request-map params] (link-to-if (inverse-condition condition) text request-map params))
    ([condition text request-map] (link-to-if (inverse-condition condition) text request-map)))