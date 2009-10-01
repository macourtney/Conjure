(ns conjure.view.base
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defmacro
#^{:doc "Defines a view. This macro should be used in a view file to define the parameters used in the view."}
  defview [params & body]
  `(defn ~'render-view [~'request-map ~@params]
    ~@body))

(defn
#^{:doc "Returns the value of :id from the given parameters. If the value of :id is a map, then this method returns the
value of :id in the map. This method is used by url-for to get the id from from the params passed to it."}
  id-from [params]
  (let [id (:id params)]
    (if (and id (map? id))
      (:id id)
      id)))

(defn-
#^{:doc "Returns the value of :anchor from the given parameters and adds a '#' before it. If the key :anchor does not 
exist in params, then this method returns nil This method is used by url-for to get the id from from the params passed 
to it."}
  anchor-from [params]
  (let [anchor (:anchor params)]
    (if anchor
      (str "#" anchor))))  

(defn
#^{:doc 
"Returns the url for the given parameters. The following parameters are valid:

     :action - The name of the action to link to.
     :controller - The name of the controller to link to.
     :id - The id to pass, or if id links to a map, then the value of :id in that map is used. (Optional)
     :anchor - Specifies the anchor name to be appended to the path."}
  url-for
  ([request-map params] (url-for (merge (select-keys request-map [:controller :action]) params))) 
  ([params]
  (let [controller (conjure-str-utils/str-keyword (:controller params))
        action (conjure-str-utils/str-keyword (:action params))]
    (if (and controller action)
      (apply str 
        (interleave 
          (repeat "/") 
          (filter #(not (nil? %))
            [controller action (id-from params) (anchor-from params)])))
      (throw (new RuntimeException (str "You must pass a controller and action to url-for. " params)))))))

(defn
#^{:doc 
"Returns a link for the given text and parameters using url-for."}
  link-to
    ([text request-map params] (link-to text (merge (select-keys request-map [:controller :action]) params)))
    ([text params]
      (str "<a href=\"" (url-for params) "\">" text "</a>")))