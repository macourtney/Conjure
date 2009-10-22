(ns conjure.view.base
  (:use clj-html.core)
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
      
(defn-
#^{:doc "Returns the params merged with the request-map. Only including the keys from request-map used by url-for"}
  merge-url-for-params [request-map params]
  (merge (select-keys request-map [:controller :action :scheme :request-method :server-name :server-port ]) params))

(defn-
#^{:doc "Returns the full host string from the given params. Used by url-for." }
  full-host [params]
  (let [server-name (:server-name params)
        scheme (:scheme params)
        user (:user params)
        password (:password params)
        port (:port params)
        server-port (:server-port params)]
    (if (and server-name (not (:only-path params)))
      (str 
        (if scheme (conjure-str-utils/str-keyword scheme) "http") "://" 
        (if (and user password) (str user ":" password "@")) 
        server-name 
        (if port 
          (str ":" port)
          (if (and server-port (not (= server-port 80))) 
            (str ":" server-port)))))))

(defn
#^{:doc 
"Returns the url for the given parameters. The following parameters are valid:

     :action - The name of the action to link to.
     :controller - The name of the controller to link to.
     :id - The id to pass, or if id links to a map, then the value of :id in that map is used. (Optional)
     :anchor - Specifies the anchor name to be appended to the path.
     :user - Inline HTTP authentication (only used if :password is also present)
     :password - Inline HTTP authentication (only use if :user is also present)
     :scheme - Overrides the default scheme. Example values: :http, :ftp
     :server-name - Overrides the default server name.
     :port - Overrides the default server port."}
  url-for
  ([request-map params] (url-for (merge-url-for-params request-map params))) 
  ([params]
  (let [controller (conjure-str-utils/str-keyword (:controller params))
        action (conjure-str-utils/str-keyword (:action params))]
    (if (and controller action)
      (apply str 
        (full-host params) 
        (interleave 
          (repeat "/") 
          (filter #(not (nil? %))
            [controller action (id-from params) (anchor-from params)])))
      (throw (new RuntimeException (str "You must pass a controller and action to url-for. " params)))))))

;(defn-
;#^{:doc "Generates the html attributes for the given options."}
;  generate-html-options [html-options]
;  (apply str 
;    (interleave
;      (repeat " ")
;      (map 
;        #(str (conjure-str-utils/str-keyword  %) "=\"" (conjure-str-utils/str-keyword (get html-options %)) "\"") 
;        (keys html-options)))))
        
(defn- #^{:doc "If function is a function, then this method evaluates it with the given args. Otherwise, it just returns
function." }
  evaluate-if-fn [function & args]
  (if (fn? function)
    (apply function args)
    function))

(defn
#^{:doc 
"Returns a link for the given text and parameters using url-for. Params has the same valid parameters as url-for, plus:

     :html-options - a map of html attributes to add to the anchor tag.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)."}
  link-to
    ([text request-map params] (link-to text (merge-url-for-params request-map params)))
    ([text params]
      (let [html-options (if (:html-options params) (:html-options params) {})]
        (htmli [:a (assoc html-options :href (url-for params)) (evaluate-if-fn text params)]))))

(defn
#^{:doc "If condition is true, then call link-to with the given text, request-map and params. If condition is false, 
then just return text. If condition is a function, it is evaluated with params merged with request-map. If text is a 
function, it is evaluated with params merged with request-map (just like link-to)." }
  link-to-if
    ([condition text request-map params] (link-to-if condition text (merge-url-for-params request-map params)))
    ([condition text params]
      (if (evaluate-if-fn condition params)
        (link-to text params)
        (evaluate-if-fn text params))))

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
    ([condition text params] (link-to-if (inverse-condition condition) text params)))

(defn
#^{:doc 
"Creates a form tag block from the given options and with the given body. If the request-map is given, it is merged into
the url options map.

If body is a function, it is passed the url options after being merged with the given request-map.

Valid options:    
    :name - The key for the params map passed to the target url. If name is not given, then the value of :controller in
        the url map is used. If :controller is not given in the url map, then \"record\" is used. 
    :url - A map for the target url of the form. Uses the same options as url-for.
    :html - The html attributes for the form tag." }
  form-for 
  ([request-map options body] (form-for (assoc options :url (merge-url-for-params request-map (:url options))) body))
  ([options body]
    (let [html-options (:html options)
          url-options (:url options)]
      (htmli 
        [:form 
          (merge 
            html-options
            { :method (or (:method html-options) "put"), 
              :action (url-for url-options),
              :name (or (:name options) (:controller url-options) "record") })
          (evaluate-if-fn body url-options)]))))

(defn-
#^{:doc "Returns the id value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  id-value [record-name-str key-name-str]
  (str record-name-str "-" key-name-str))
  
(defn-
#^{:doc "Returns the name value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  name-value [record-name-str key-name-str]
  (str record-name-str "[" key-name-str "]"))

(defn
#^{:doc "Creates an input tag of the given type for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  input [input-type record-name key-name record html-options]
    (let [record-name-str (conjure-str-utils/str-keyword record-name)
          key-name-str (conjure-str-utils/str-keyword key-name)]
      (htmli 
        [:input 
          (merge 
            html-options
            { :type (conjure-str-utils/str-keyword input-type),
              :id (id-value record-name-str key-name-str), 
              :name (name-value record-name-str key-name-str)
              :value (get record key-name) })])))

(defn
#^{:doc "Creates an input tag of type text for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  text-field
  ([record-name key-name record] (text-field record-name key-name record {})) 
  ([record-name key-name record html-options]
    (input :text record-name key-name record html-options)))
            
(defn
#^{:doc "Creates a text area tag for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  text-area 
  ([record-name key-name record] (text-area record-name key-name record {}))
  ([record-name key-name record html-options]
    (let [record-name-str (conjure-str-utils/str-keyword record-name)
          key-name-str (conjure-str-utils/str-keyword key-name)]
      (htmli 
        [:textarea 
          (merge
            { :rows 40, :cols 20 }
            html-options
            { :id (id-value record-name-str key-name-str),
              :name (name-value record-name-str key-name-str) })
          (get record key-name) ]))))

(defn
#^{:doc "Creates an input tag of type \"hidden\" for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  hidden-field 
  ([record-name key-name record] (hidden-field record-name key-name record {}))
  ([record-name key-name record html-options]
    (input :hidden record-name key-name record html-options)))

(defn
#^{:doc "Creates a set of select option tags from one of the following: a pair of strings representing the name and value.  A name, value and selection boolean.  Or a list of options (the name and value are the same)."}
option-tag
([option-name value-name selected] 
   (htmli [:option (merge {:value value-name} 
			  (if selected {:selected "true"} {})) option-name]))
([name-seq] (str-utils/str-join "\n" (map option-tag name-seq name-seq)))
([name value](option-tag name value false)))

