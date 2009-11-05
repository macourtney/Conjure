(ns conjure.view.base
  (:use clj-html.core)
  (:require [clojure.contrib.str-utils :as str-utils]
            environment
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
  
(defn
#^{:doc "Returns the name value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  name-value [record-name-str key-name-str]
  (str record-name-str "[" key-name-str "]"))

(defn
#^{:doc "Creates an input tag of the given type for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  input [input-type record record-name key-name html-options]
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
  ([record record-name key-name] (text-field record record-name key-name {})) 
  ([record record-name key-name html-options]
    (input :text record record-name key-name html-options)))
            
(defn
#^{:doc "Creates a text area tag for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  text-area 
  ([record record-name key-name] (text-area record record-name key-name {}))
  ([record record-name key-name html-options]
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
#^{:doc "Creates an input tag of type \"hidden\" for a field of name key-name in record of the given name. You can pass
along an optional option map for the html options." }
  hidden-field 
  ([record record-name key-name] (hidden-field record record-name key-name {}))
  ([record record-name key-name html-options]
    (input :hidden record record-name key-name html-options)))

(defn
#^{:doc "Creates a select option tag from one of the following: A name, value and selection boolean, or a map 
containing a name, value (optional), and selected (optional) keys."}
  option-tag
  ([option-name value-name selected] 
    (htmli [:option (merge {:value value-name} 
      (if selected {:selected "true"} {})) option-name]))
  ([option-name option-map]
    (let [option-name-str (conjure-str-utils/str-keyword option-name)]
      (option-tag option-name-str (or (:value option-map) option-name-str) (or (:selected option-map) false)))))
      
(defn
#^{:doc "Returns a string containing a list of options using option-tag. The given option-map contains a maping from 
option names to option-tag option maps."}
  option-tags [option-map]
  (apply str (map option-tag (keys option-map) (vals option-map))))
  
(defn
#^{ :doc "Creates an option map from the seq of record in the given map. Map options include:

  :records - The seq of records to use as options.
  :name-key - The key in each record who's value will be used as the name of each option. If this key does not exist, then :name is used.
  :value-key - The key in each record who's value will be used as the value of each option. If this key does not exist, then :id is used.
  :blank - If true, adds a blank option (name = \"\", value = \"\"). Default is false." }
  options-from-records 
  ([record-map] 
    (let [name-key (get record-map :name-key :name)
          value-key (get record-map :value-key :id)]
      (apply merge
        (cons
          (if (:blank record-map) { "" { :value "" } }) 
          (map 
            (fn [record] { (get record name-key) { :value (get record value-key) } }) 
            (get record-map :records [])))))))

(defn-
#^{ :doc "Augments the given html-options with a record name option." }
  record-html-options [html-options record-name key-name]
  (assoc html-options
    :name (name-value (conjure-str-utils/str-keyword record-name) (conjure-str-utils/str-keyword key-name))))

(defn
#^{ :doc "Returns record-key if the value of record-key in option-map equals record-value. If option-map does not 
contain record-key then this method returns record-key if record-key equals record-value." }
  is-value-key? [option-map option-key value]
  (if (= (or (get option-map option-key) (conjure-str-utils/str-keyword option-key)) value)
    option-key
    nil))

(defn
#^{ :doc "Augments the given option-map setting selected for the option with the value of record-value." }
  option-map-select-value [option-map value]
  (let [option-key (some #(is-value-key? option-map % value) (keys option-map))]
    (if option-key
      (assoc option-map option-key (assoc (or (get option-map option-key) { :value (conjure-str-utils/str-keyword option-key) } ) :selected true))
      option-map)))

(defn
#^{ :doc "Creates a select tag using the given select-options or record info and select-options." }
  select-tag
  ([select-options]
    (htmli [:select (:html-options select-options) (option-tags (:option-map select-options))]))
  ([record record-name key-name select-options]
    (select-tag
      { :html-options (record-html-options (:html-options select-options) record-name key-name)
        :option-map (option-map-select-value (:option-map select-options) (get record key-name)) })))

(defn
#^{ :doc "Returns the full path to the given image source." }
  image-path [source]
  (if (. source startsWith "/")
    source
    (if (. source startsWith "http://") ; This should probably check for ftp, https, and etc.
      source
      (str "/" environment/images-dir "/" source))))
  
(defn
#^{ :doc "Returns an image tag for the given source and with the given options." }
  image-tag 
  ([source] (image-tag source {}))
  ([source options] (htmli [:img (merge options { :src (image-path source) })])))