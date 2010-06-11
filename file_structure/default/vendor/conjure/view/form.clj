(ns conjure.view.form)

(in-ns 'conjure.view.base)

(require ['clj-html.helpers :as 'helpers])
(require ['clojure.contrib.logging :as 'logging])
(require ['conjure.model.util :as 'model-util])
(require ['conjure.server.request :as 'request])
(require ['conjure.util.map-utils :as 'map-utils])
(require ['conjure.view.util :as 'view-utils])

(defn
#^{:doc "Returns the name value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  name-value [record-name-str key-name-str]
  (str record-name-str "[" key-name-str "]"))

(require 'conjure.view.select)

(defn
#^{:doc 
"Creates a form tag block from the given options and with the given body. If options is given, it is merged into
the request-map.

The request-map for the body will be merged with the given options.

Options has the same options as url-for plus the following options:    
    :name - The key for the params map passed to the target url. If name is not given, then the value of :controller in
        the request map is used. If :controller is not given in the request map, then \"record\" is used. 
    :html-options - The html attributes for the form tag." }
  form-for 
  ([body] (form-for {} body))
  ([options body]
    (let [html-options (:html-options options)
          action (or (:action html-options) (view-utils/url-for options))]
      [:form 
        (merge 
          html-options
          { :method (or (:method html-options) "post"), 
            :action action,
            :name (or (:name options) (:controller options) (request/controller) "record") })
        (request/with-request-map-fn #(view-utils/merge-url-for-params % options)
          (evaluate-if-fn body))])))

(defn-
#^{:doc "Returns the id value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  id-value [record-name-str key-name-str]
  (str record-name-str "-" key-name-str))

(defn
#^{:doc "Creates an input tag of the given type for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  input [input-type record record-name key-name html-options]
    (let [record-name-str (conjure-str-utils/str-keyword record-name)
          key-name-str (conjure-str-utils/str-keyword key-name)]
      [:input 
        (merge
          { :type (conjure-str-utils/str-keyword input-type),
            :id (id-value record-name-str key-name-str), 
            :name (name-value record-name-str key-name-str)
            :value (helpers/h (get record key-name)) } 
          html-options)]))

(defn
#^{:doc "Creates an input tag of type text for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  text-field
  ([record record-name key-name] (text-field record record-name key-name {})) 
  ([record record-name key-name html-options]
    (input :text record record-name key-name html-options)))

(defn
#^{:doc "Creates an input tag of type password for a field of name key-name in record of the given name. You can pass 
along an optional option map for the html options." }
  password-field
  ([record record-name key-name] (password-field record record-name key-name {}))
  ([record record-name key-name html-options]
    (input :password record record-name key-name html-options)))

(defn
#^{:doc "Creates a text area tag for a field of name key-name in record of the given name. You can pass along
an optional option map for the html options." }
  text-area 
  ([record record-name key-name] (text-area record record-name key-name {}))
  ([record record-name key-name html-options]
    (let [record-name-str (conjure-str-utils/str-keyword record-name)
          key-name-str (conjure-str-utils/str-keyword key-name)]
      [:textarea 
        (merge
          { :rows 40, :cols 20 }
          html-options
          { :id (id-value record-name-str key-name-str),
            :name (name-value record-name-str key-name-str) })
        (helpers/h (get record key-name)) ])))

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
  ([value html-options] [:input (merge html-options { :type "submit", :value value, :name "button" })]))

(defn
#^{ :doc "Returns a check box tag from the given record, record name, and key for the record. Note: browsers will send 
nothing if a check box is not checked, therefore this function also creates a hidden field with the unchecked value." }
  check-box 
  ([record record-name key-name] (check-box record record-name key-name {}))
  ([record record-name key-name html-options] (check-box record record-name key-name html-options 1))
  ([record record-name key-name html-options checked-value] 
    (check-box record record-name key-name html-options checked-value 0))
  ([record record-name key-name html-options checked-value unchecked-value]
    (list
      (input :checkbox record record-name key-name (merge html-options { :value (str checked-value) }))
      (hidden-field record record-name key-name (merge html-options { :value (str unchecked-value) })))))

(defn
#^{ :doc "Returns a radio button tag for the given record, record name and key for the record." }
  radio-button 
  ([record record-name key-name value] (radio-button record record-name key-name value {}))
  ([record record-name key-name value html-options]
    (let [str-value (if value (helpers/h value))]
      (input :radio record record-name key-name 
        (merge
          (map-utils/drop-nils
            { :value (if value (helpers/h value)), 
              :checked (if (= (get record key-name) value) "checked") })
          html-options)))))

(defn
#^{ :doc "Creates a form with a single input of type button for use when you only need a button somewhere.

Supported options:
  :html-options - The html options of the button." }
  button-to 
  ([text] (button-to text {}))
  ([text params]
    (form-for (dissoc params :html-options)
      (form-button (evaluate-if-fn text) (:html-options params)))))