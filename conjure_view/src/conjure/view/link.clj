(ns conjure.view.link)
  
(in-ns 'conjure.view.base)

(require ['conjure.util.request :as 'request])
(require ['conjure.util.conjure-utils :as 'conjure-utils])

(defn
#^{ :doc "Returns the attributes for the link tag (\"a\" tag) from the request-map." }
  a-attributes [params]
  (let [html-options (or (:html-options params) {})]
    (if (:href html-options)
      html-options
      (assoc html-options :href (conjure-utils/url-for params)))))

(defn evaluate-if-fn-with-params [params function & fn-params]
  (request/with-request-map-fn #(conjure-utils/merge-url-for-params % params)
    (apply evaluate-if-fn function fn-params)))

(defn
#^{ :doc 
"Returns a link for the given text and parameters using url-for. Params has the same valid parameters as url-for, plus:

     :html-options - a map of html attributes to add to the anchor tag. If html-options contains a :href key, the value 
                     override the href generated from params.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  link-to
  ([text] (link-to text {}))
  ([text params] [:a (a-attributes params) (evaluate-if-fn-with-params params text)]))

(defn
#^{:doc "If condition is true, then call link-to with the given text, request-map and params. If condition is false, 
then just return text. If condition is a function, it is evaluated with params merged with request-map. If text is a 
function, it is evaluated with params merged with request-map (just like link-to)." }
  link-to-if
  ([condition text] (link-to-if condition text {}))
  ([condition text params]
    (if (evaluate-if-fn-with-params params condition)
      (link-to text params)
      (evaluate-if-fn-with-params params text))))

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
    ([condition text params] (link-to-if (inverse-condition condition) text params))
    ([condition text] (link-to-if (inverse-condition condition) text)))

(defn
#^{ :doc "Returns the url for the page the user navigated from. Or nil if there is no referrer." }
  back-url []
  (request/referrer))

(defn
#^{ :doc "Links to the page the user navigated from." }
  link-back
  ([text] (link-back text {}))
  ([text html-options]
    [:a (merge { :href (or (back-url) "#") } html-options) (evaluate-if-fn text)]))