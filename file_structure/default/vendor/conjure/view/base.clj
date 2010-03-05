(ns conjure.view.base
  (:use clj-html.core)
  (:require [clj-html.helpers :as helpers]
            [clojure.contrib.json.write :as json-write]
            [clojure.contrib.str-utils :as str-utils]
            [com.reasonr.scriptjure :as scriptjure]
            [conjure.util.javascript-utils :as javascript-utils]
            [conjure.model.util :as model-util]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.util.html-utils :as html-utils]
            [conjure.view.util :as view-utils]
            environment))

(defmacro
#^{ :doc "Defines a view. This macro should be used in a view file to define the parameters used in the view." }
  defview [params & body]
  `(defn ~'render-view [~'request-map ~@params]
    ~@body))

(defn- 
#^{ :doc "If function is a function, then this method evaluates it with the given args. Otherwise, it just returns
function." }
  evaluate-if-fn [function & args]
  (if (fn? function)
    (apply function args)
    function))

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
    (htmli [:a (a-attributes request-map) (evaluate-if-fn text request-map)])))

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

(defn
#^{:doc 
"Creates a form tag block from the given options and with the given body. If options is given, it is merged into
the request-map.

If body is a function, it is passed the request-map after being merged with the given options.

Options has the same options as url-for plus the following options:    
    :name - The key for the params map passed to the target url. If name is not given, then the value of :controller in
        the url map is used. If :controller is not given in the url map, then \"record\" is used. 
    :html-options - The html attributes for the form tag." }
  form-for 
  ([request-map options body] (form-for (view-utils/merge-url-for-params request-map options) body))
  ([request-map body]
    (let [html-options (:html-options request-map)
          action (or (:action html-options) (view-utils/url-for request-map))]
      (htmli 
        [:form 
          (merge 
            html-options
            { :method (or (:method html-options) "post"), 
              :action action,
              :name (or (:name request-map) (:controller request-map) "record") })
          (evaluate-if-fn body request-map)]))))

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
              :value (helpers/h (get record key-name)) } 
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
          (helpers/h (get record key-name)) ]))))

(defn
#^{ :doc "Creates an input tag of type \"hidden\" for a field of name key-name in record of the given name. You can pass
along an optional option map for the html options." }
  hidden-field 
  ([record record-name key-name] (hidden-field record record-name key-name {}))
  ([record record-name key-name html-options]
    (input :hidden record record-name key-name html-options)))

(defn
#^{ :doc "Creates an input tag for a submit button with the given value." }
  form-button
  ([value] (form-button value {})) 
  ([value html-options] (htmli [:input (merge html-options { :type "submit", :value value, :name "button" })])))

(defn
#^{:doc "Creates a select option tag from one of the following: A name, value and selection boolean, or a map 
containing a name, value (optional), and selected (optional) keys."}
  option-tag
  ([option-name value-name selected] 
    (htmli [:option (merge {:value value-name} 
      (if selected {:selected "true"} {})) (if (and option-name (> (. option-name length) 0)) option-name "&lt;blank&gt;")]))
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
            (fn [record] { (or (get record name-key) (get record value-key)) { :value (helpers/h (get record value-key)) } }) 
            (get record-map :records [])))))))

(defn
#^{ :doc "Creates an option map from the model in the given map. Options include:

  :model - The name of the model to pull the records from.
  :name-key - The key in each record who's value will be used as the name of each option. If this key does not exist, then :name is used.
  :value-key - The key in each record who's value will be used as the value of each option. If this key does not exist, then :id is used.
  :blank - If true, adds a blank option (name = \"\", value = \"\"). Default is false." }
  options-from-model [option-map]
  (let [model (:model option-map)
        model-namespace (model-util/model-namespace model)
        model-namespace-symbol (symbol model-namespace)
        find-records-str (str "(" model-namespace "/find-records [true])")]
    (do
      (require model-namespace-symbol)
      (options-from-records (assoc option-map :records (eval (read-string find-records-str)))))))

(defn-
#^{ :doc "Augments the given html-options with a record name option." }
  record-html-options [html-options record-name key-name]
  (assoc html-options
    :name (name-value (conjure-str-utils/str-keyword record-name) (conjure-str-utils/str-keyword key-name))))

(defn
#^{ :doc "Returns option-key if the value of option-key in option-map equals value. If option-map does not 
contain option-key then this method returns option-key if option-key equals value." }
  is-value-key? [option-map option-key value]
  (let [map-value (get option-map option-key)
        final-map-value (if (map? map-value) (:value map-value) map-value)]
    (if (= (or final-map-value (conjure-str-utils/str-keyword option-key)) value)
      option-key
      nil)))

(defn
#^{ :doc "Augments the given option-map setting selected for the option with the value of record-value." }
  option-map-select-value [option-map value]
  (let [option-key (some #(is-value-key? option-map % value) (keys option-map))]
    (if option-key
      (let [map-value (get option-map option-key)
            final-map-value 
              (if (and map-value (not (map? map-value)))
                { :value map-value } 
                map-value)]
        (assoc option-map option-key 
          (assoc 
            (or final-map-value 
              { :value (conjure-str-utils/str-keyword option-key) } ) 
              :selected 
              true)))
      option-map)))

(defn
#^{ :doc "Creates a select tag using the given select-options or record info and select-options." }
  select-tag
  ([select-options]
    (htmli [:select (:html-options select-options) (option-tags (:option-map select-options))]))
  ([record record-name key-name select-options]
    (select-tag
      { :html-options (record-html-options (:html-options select-options) record-name key-name)
        :option-map (option-map-select-value (:option-map select-options) (helpers/h (get record key-name))) })))

(defn
#^{ :doc "Creates a form with a single input of type button for use when you only need a button somewhere.

Supported options:
  :html-options - The html options of the button." }
  button-to 
  ([text request-map params] (button-to text (view-utils/merge-url-for-params request-map params)))
  ([text request-map]
    (form-for (dissoc request-map :html-options)
      (form-button (evaluate-if-fn text request-map) (:html-options request-map)))))

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
          html-options) ""])))

(defn
#^{ :doc "Returns a jquery javascript include tag with the optional given options." } 
  jquery-include-tag
  ([] (jquery-include-tag {}))
  ([html-options]
    (javascript-include-tag environment/jquery html-options)))
    
(defn
#^{ :doc "Returns a jquery javascript include tag with the optional given options." } 
  conjure-js-include-tag
  ([] (conjure-js-include-tag {}))
  ([html-options]
    (javascript-include-tag environment/conjure-js html-options)))

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

(defn
#^{ :doc "Returns an xml header tag with the given html-options. If no html-options are given, then the tag is created 
with the following defaults:

  version=\"1.0\"" }
  xml-header-tag 
  ([] (xml-header-tag {}))
  ([html-options]
    (str 
      "<?xml " 
      (html-utils/attribute-list-str (merge { :version "1.0" } html-options ))
      "?>")))

(defn
#^{ :doc "Returns the html doc type tag. You can pass a type into this method for a specific type. Valid types are:

  :html4.01-strict
  :html4.01-transitional
  :html4.01-frameset
  :xhtml1.0-strict
  :xhtml1.0-transitional - default
  :xhtml1.0-frameset
  :xhtml1.1" }
  html-doctype
  ([] (html-doctype :xhtml1.0-transitional)) 
  ([doc-type]
    (cond
      (= doc-type :html4.01-strict) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
      (= doc-type :html4.01-transitional) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
      (= doc-type :html4.01-frameset) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">"
      (= doc-type :xhtml1.0-strict) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      (= doc-type :xhtml1.0-transitional) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
      (= doc-type :xhtml1.0-frameset) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"
      (= doc-type :xhtml1.1) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
      true (throw (RuntimeException. (str "Unknown doc type: " doc-type))))))

(defn-
#^{ :doc "Returns the position function for the given position." }
  position-function [position]
  (cond 
    (= position :content) 'ajaxContent
    (= position :replace) 'ajaxReplace
    (= position :before) 'ajaxBefore
    (= position :after) 'ajaxAfter
    (= position :top) 'ajaxTop
    (= position :bottom) 'ajaxBottom
    (= position :remove) 'ajaxRemove))
    
(defn
#^{ :doc "Creates a link-to-remote-onclick success function which adds the returned content to the tag with the given 
id based on position. Position can be one of the following:

  :content - Replaces the entire contents of success-id (default)
  :replace - Replaces success-id
  :before - Adds the content before success-id
  :after - Adds the content after success-id
  :top - Adds the content in the first position in success-id
  :bottom - Adds the content in the last position in success-id
  :remove - Removes success-id" }
  success-fn 
  ([success-id] (success-fn success-id :content))
  ([success-id position]
    (scriptjure/quasiquote
      ((clj (position-function position)) (clj (str "#" success-id))))))

(defn
#^{ :doc "Creates a standard link-to-remote-onclick error function which simply displays the returned error." }
  error-fn []
  'ajaxError)

(defn
#^{ :doc "Creates a standard confirm dialog with the given message." }
  confirm-fn [message]
  (scriptjure/quasiquote (ajaxConfirm (clj message))))
  
(defn-
#^{ :doc "Generates the ajax map for the given params. Valid params are:

    :method - The method to use for the ajax call. Default is \"POST\"
    :ajax-url - The url for the ajax request to call instead of creating a url from the given controller and action.
    :update - A scriptjure function or a map. If it is a function, then it is called when the ajax request returns with 
              success. If it is a map, then the scriptjure function value of :success is called when the ajax request 
              returns successfully, and the scriptjure function value of :error is called when the ajax request fails.
    :confirm - A scriptjure function to call to confirm the action before the ajax call is executed." }
  ajax-map [request-map]
  (let [ajax-type (or (:method request-map) "POST")
        url (or (:ajax-url request-map) (view-utils/url-for request-map))
        update (:update request-map)
        success-fn (if (map? update) (:success update) update)
        error-fn (if (and (map? update) (contains? update :error)) (:error update) (error-fn))
        confirm-fn (:confirm request-map)]

    (scriptjure/quasiquote 
      { :type (clj ajax-type)
        :url (clj url)
        :dataType "html"
        :success (clj success-fn)
        :error (clj error-fn)
        :confirm (clj confirm-fn) })))

(defn 
#^{ :doc 
"Returns an ajax link for the given text and parameters using url-for. Params has the same valid parameters as url-for, 
plus:

     :update - A map or a scriptjure function. If the value is a function, then it is called when the ajax request 
               succeeds.
               If the value is a map then it looks for the following keys: 
                  :success - The id of the element to update if the request succeeds.
                  :failure - The id of the element to update if the request fails.
     :method - The request method. Possible values POST, GET, PUT, DELETE. However, not all browsers support PUT and 
               DELETE. Default is POST.
     :confirm - a method to call before the ajax call to get a confirmation from the user.
     :html-options - a map of html attributes to add to the anchor tag.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  ajax-link-to
  ([text request-map params] (ajax-link-to text (view-utils/merge-url-for-params request-map params)))
  ([text request-map]
    (let [html-options (or (:html-options request-map) {})
          id (or (:id html-options) (str "id-" (rand-int 1000000)))
          id-string (str "#" id)
          ajax-function (ajax-map request-map)]
      (htmli 
        [:a 
          (merge html-options 
            { :href (or (:href html-options) "#")
              :id id })
          (evaluate-if-fn text request-map)]
        [:script { :type "text/javascript" } 
          (scriptjure/js
            (ajaxClick (clj id-string) (clj ajax-function)))]))))

(defn
#^{ :doc 
"Returns an ajax form for with the given body. Params has the same valid parameters as form-for, 
plus:

     :update - The id of the element to update. If the value is a map then it looks for the following keys: 
                  :success - The id of the element to update if the request succeeds.
                  :failure - The id of the element to update if the request fails.
     :method - The request method. Possible values POST, GET, PUT, DELETE. However, not all browsers support PUT and 
               DELETE. Default is POST.
     :confirm - a method to call before the ajax call to get a confirmation from the user.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  ajax-form-for
  ([request-map options body] 
    (ajax-form-for (view-utils/merge-url-for-params request-map options) body))
  ([request-map body]
    (let [html-options (or (:html-options request-map) {})
          id (or (:id html-options) (str "id-" (rand-int 1000000)))
          id-string (str "#" id)
          ajax-function (ajax-map request-map)
          form-for-options (assoc request-map :html-options (merge html-options { :id id }))]
      (str 
        (form-for form-for-options body)
        (htmli
          [:script { :type "text/javascript" } 
            (scriptjure/js
              (ajaxSubmit (clj id-string) (clj ajax-function)))])))))