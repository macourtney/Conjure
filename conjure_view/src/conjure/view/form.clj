(ns conjure.view.form)

(in-ns 'conjure.view.base)


(require ['conjure.model.util :as 'model-util])
(require ['clojure.tools.logging :as 'logging])
(require ['clojure.tools.map-utils :as 'map-utils])
(require ['conjure.util.conjure-utils :as 'conjure-utils])
(require ['conjure.util.request :as 'request])

(defn
#^{:doc "Returns the name value for the given record name and key name. Note, both record-name-str and key-name-str must 
be strings." }
  name-value [record-name-str key-name-str]
  (str record-name-str "[" key-name-str "]"))

(defn
#^{:doc 
"Creates a form tag block from the given options and with the given body. If options is given, it is merged into
the request-map.

The request-map for the body will be merged with the given options.

Options has the same options as url-for plus the following options:    
    :name - The key for the params map passed to the target url. If name is not given, then the value of :service in
        the request map is used. If :service is not given in the request map, then \"record\" is used. 
    :html-options - The html attributes for the form tag." }
  form-for 
  ([body] (form-for {} body))
  ([options body]
    (let [html-options (:html-options options)
          action (or (:action html-options) (conjure-utils/url-for options))]
      [:form 
        (merge 
          html-options
          { :method (or (:method html-options) "post"), 
            :action action,
            :name (or (:name options) (:service options) (request/service) "record") })
        (request/with-request-map-fn #(conjure-utils/merge-url-for-params % options)
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
            :value (get record key-name) } 
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
        (get record key-name) ])))

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
  ([value html-options]
    [:button
      (merge { :type "submit", :value (conjure-str-utils/str-keyword value), :name "button" } html-options)
      (conjure-str-utils/str-keyword value)]))

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
    (input :radio record record-name key-name 
           (merge
             (map-utils/drop-nils
               { :value value, 
                 :checked (if (= (get record key-name) value) "checked") })
             html-options))))

(defn
#^{ :doc "Creates a form with a single input of type button for use when you only need a button somewhere.

Supported options:
  :html-options - The html options of the button." }
  button-to 
  ([text] (button-to text {}))
  ([text params]
    (let [options (dissoc params :html-options)
          button-text (request/with-request-map-fn #(conjure-utils/merge-url-for-params % options) (evaluate-if-fn text))]
      (form-for options
        (form-button button-text (:html-options params))))))