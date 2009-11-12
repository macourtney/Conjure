(ns conjure.view.base
  (:use clj-html.core)
  (:require [clojure.contrib.str-utils :as str-utils]
            environment
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.util.html-utils :as html-utils]
            [conjure.view.util :as view-utils]))

(defmacro
#^{:doc "Defines a view. This macro should be used in a view file to define the parameters used in the view."}
  defview [params & body]
  `(defn ~'render-view [~'request-map ~@params]
    ~@body))

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
  ([text request-map params] (link-to text (view-utils/merge-url-for-params request-map params)))
  ([text params]
    (let [html-options (if (:html-options params) (:html-options params) {})]
      (htmli [:a (assoc html-options :href (view-utils/url-for params)) (evaluate-if-fn text params)]))))

(defn
#^{:doc "If condition is true, then call link-to with the given text, request-map and params. If condition is false, 
then just return text. If condition is a function, it is evaluated with params merged with request-map. If text is a 
function, it is evaluated with params merged with request-map (just like link-to)." }
  link-to-if
  ([condition text request-map params] (link-to-if condition text (view-utils/merge-url-for-params request-map params)))
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
  ([request-map options body] (form-for (assoc options :url (view-utils/merge-url-for-params request-map (:url options))) body))
  ([options body]
    (let [html-options (:html options)
          url-options (:url options)]
      (htmli 
        [:form 
          (merge 
            html-options
            { :method (or (:method html-options) "put"), 
              :action (view-utils/url-for url-options),
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
            { :type (conjure-str-utils/str-keyword input-type),
              :id (id-value record-name-str key-name-str), 
              :name (name-value record-name-str key-name-str)
              :value (get record key-name) } 
            html-options)])))

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

(defn-
#^{ :doc "Replaces the current extension on source with the given extension." }
  replace-extension [source extension]
  (if extension
    (conjure-str-utils/add-ending-if-absent
      (str-utils/re-sub #"\.[a-zA-Z0-9]*$" "" source) 
      (if extension (str "." extension)))
    source))

(defn
#^{ :doc "Returns a path for the given source in the given base-dir with the given extension (if none is given)." }
  compute-public-path 
  ([source base-dir] (compute-public-path source base-dir nil))
  ([source base-dir extension]
    (replace-extension
      (if (. source startsWith "/")
        source
        (if (. source startsWith "http://") ; This should probably check for ftp, https, and etc.
          source
          (str "/" base-dir "/" source)))
      extension)))

(defn
#^{ :doc "Returns the full path to the given image source." }
  image-path [source]
    (compute-public-path source environment/images-dir))
  
(defn
#^{ :doc "Returns an image tag for the given source and with the given options." }
  image-tag 
  ([source] (image-tag source {}))
  ([source html-options] (htmli [:img (merge { :src (image-path source) } html-options)])))

(defn
#^{ :doc "Returns the full path to the given stylesheet source." }
  stylesheet-path [source]
    (compute-public-path source environment/stylesheets-dir "css"))

(defn-
#^{ :doc "Returns the type of the first parameter." }
  first-type [& params]
  (class (first params)))

(defmulti 
#^{ :doc "Returns a stylesheet tag for the given source and with the given options." }
  stylesheet-link-tag first-type)
  
(defmethod stylesheet-link-tag clojure.lang.PersistentVector
  ([sources] (stylesheet-link-tag sources {}))
  ([sources html-options]
    (apply str (map stylesheet-link-tag sources (repeat html-options)))))
  
(defmethod stylesheet-link-tag String
  ([source] (stylesheet-link-tag source {}))
  ([source html-options]
    (htmli
      [:link 
        (merge 
          { :href (stylesheet-path source), 
            :media "screen", 
            :rel "stylesheet", 
            :type "text/css" } 
          html-options)])))

(defn
#^{ :doc "Returns the full path to the given javascript source." }
  javascript-path [source]
    (compute-public-path source environment/javascripts-dir "js"))

(defmulti
#^{ :doc "Returns a javascript include tag for the given source and with the given options." }
  javascript-include-tag first-type)

(defmethod javascript-include-tag clojure.lang.PersistentVector
  ([sources] (javascript-include-tag sources {}))
  ([sources html-options]
    (apply str (map javascript-include-tag sources (repeat html-options)))))

(defmethod javascript-include-tag String
  ([source] (javascript-include-tag source {}))
  ([source html-options]
    (htmli
      [:script
        (merge 
          { :src (javascript-path source),
            :type "text/javascript" } 
          html-options)])))

(defn
#^{ :doc "Returns a mailto link with the given mail options. Valid mail options are:

  :address - The full e-mail address to use. (required)
  :name - The display name to use. If not given, address is used.
  :html-options - Any extra attributes for the mail to tag.
  :replace-at - If name is not given, then replace the @ symbol with this text in the address before using it as the name.
  :replace-dot - If name is not given, then replace the . in the email with this text in the address before using it as the name.
  :subject - Presets the subject line of the e-mail.
  :body - Presets the body of the email.
  :cc - Carbon Copy. Adds additional recipients to the email.
  :bcc - Blind Carbon Copy. Adds additional hidden recipients to the email." }
  mail-to [mail-options] 
   (let [address (:address mail-options)
         display-name 
          (or 
            (:name mail-options) 
            (conjure-str-utils/str-replace-if address { "@" (:replace-at mail-options), "." (:replace-dot mail-options) }))
         mailto-params (html-utils/url-param-str (select-keys mail-options [:cc :bcc :subject :body]))]
     (htmli
       [:a
         (merge
           { :href (str "mailto:" address mailto-params) }
           (:html-options mail-options))
         display-name])))
         
(defn
#^{ :doc "Returns a check box tag from the given record, record name, and key for the record. Note: browsers will send 
nothing if a check box is not checked, therefore this function also creates a hidden field with the unchecked value." }
  check-box 
  ([record record-name key-name] (check-box record record-name key-name {}))
  ([record record-name key-name html-options] (check-box record record-name key-name html-options 1))
  ([record record-name key-name html-options checked-value] 
    (check-box record record-name key-name html-options checked-value 0))
  ([record record-name key-name html-options checked-value unchecked-value]
    (str 
      (input :checkbox record record-name key-name (merge html-options { :value (str checked-value) }))
      (hidden-field record record-name key-name (merge html-options { :value (str unchecked-value) })))))

(defn
#^{ :doc "Returns a radio button tag for the given record, record name and key for the record." }
  radio-button 
  ([record record-name key-name value] (radio-button record record-name key-name value {}))
  ([record record-name key-name value html-options]
    (input :radio record record-name key-name 
      (merge html-options { :value (str value), :checked (if (= (get record key-name) value) "checked") }))))