(ns conjure.view.form)

(in-ns 'conjure.view.base)

(require ['conjure.model.util :as 'model-util])
(require ['clj-html.helpers :as 'helpers])
(require ['conjure.view.util :as 'view-utils])

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
#^{ :doc "Creates a form with a single input of type button for use when you only need a button somewhere.

Supported options:
  :html-options - The html options of the button." }
  button-to 
  ([text request-map params] (button-to text (view-utils/merge-url-for-params request-map params)))
  ([text request-map]
    (form-for (dissoc request-map :html-options)
      (form-button (evaluate-if-fn text request-map) (:html-options request-map)))))