(ns conjure.view.select)

(in-ns 'conjure.view.base)

(require ['clojure.contrib.ns-utils :as 'ns-utils])

(defn
#^{ :doc "Returns the name of the given option. Option can be a keyword, string
or map. If option is a map, then the value of :name is returned." }
  option-name [option]
  (conjure-str-utils/str-keyword 
    (if (map? option)
      (:name option)
      option)))

(defn 
#^{ :doc "Returns the value of the given option. The option can be a keyword, 
string or map. If the option is a map, the value is either the value of :value
or :name depending on which is set." }
  option-value [option]
  (conjure-str-utils/str-keyword 
    (if (map? option)
      (or (:value option) (:name option))
      option)))

(defn
#^{ :doc "Returns true if the given option is selected. False otherwise." }
  option-selected? [option]
  (if (map? option)
    (:selected option)
    false))

(defn
#^{ :doc "If the given option is a map then it is returned, otherwise it's 
converted to a map with both the :name and :value set to option." }
  options-as-map [option]
  (if (map? option)
    option
    { :name option, :value option }))

(defn
#^{:doc "Creates a select option tag from one of the following: A name, value and selection boolean, or a map 
containing a name, value (optional), and selected (optional) keys."}
  option-tag
  ([option-name value-name selected] 
    [:option (merge {:value value-name} 
      (if selected { :selected "true" } {})) 
      (if (and option-name (> (. option-name length) 0))
        option-name
        "&lt;blank&gt;")])
  ([option]
    (option-tag (option-name option) (option-value option) (option-selected? option))))
      
(defn
#^{:doc "Returns a string containing a list of options using option-tag. The given option-map contains a maping from 
option names to option-tag option maps."}
  option-tags [options]
  (map option-tag options))

(defn
  option-from-record [record name-key value-key]
  (let [value (helpers/h (get record value-key))
        name (get record name-key)]
    { :name (if name (helpers/h name) value)
      :value value }))

(defn
#^{ :doc "Creates an option map from the seq of record in the given map. Map options include:

  :records - The seq of records to use as options.
  :name-key - The key in each record who's value will be used as the name of each option. If this key does not exist, then :name is used.
  :value-key - The key in each record who's value will be used as the value of each option. If this key does not exist, then :id is used.
  :blank - If true, adds a blank option (name = \"\", value = \"\"). Default is false." }
  options-from-records 
    [{ :keys [records name-key value-key blank] 
      :or { records [], name-key :name, value-key :id, blank false }
      :as record-map }]
  (filter identity
    (cons 
      (if blank { :name "", :value "" })
      (map #(option-from-record % name-key value-key) records))))

(defn
#^{ :doc "Creates an option map from the model in the given map. Options include:

  :model - The name of the model to pull the records from.
  :name-key - The key in each record who's value will be used as the name of each option. If this key does not exist, then :name is used.
  :value-key - The key in each record who's value will be used as the value of each option. If this key does not exist, then :id is used.
  :blank - If true, adds a blank option (name = \"\", value = \"\"). Default is false." }
  options-from-model [{ :keys [model] :as option-map }]
  (let [model-namespace (model-util/model-namespace model)
        model-namespace-symbol (symbol model-namespace)]
    (do
      (require model-namespace-symbol)
      (options-from-records 
        (assoc option-map :records 
          ((ns-resolve (ns-utils/get-ns model-namespace-symbol) 'find-records)
            [true]))))))

(defn-
#^{ :doc "Augments the given html-options with a record name option." }
  record-html-options [html-options record-name key-name]
  (assoc html-options
    :name 
      (name-value
        (conjure-str-utils/str-keyword record-name)
        (conjure-str-utils/str-keyword key-name))))

(defn
#^{ :doc "Sets the given option as selected if the value of the option is equal
to the given value." }
  option-select-if [option value]
  (if (= (option-value option) value) 
    (assoc (options-as-map option) :selected true)
    option))

(defn
#^{ :doc "Augments the given option-map setting selected for the option with the value of record-value." }
  options-select-value [options value]
  (map #(option-select-if % value) options))

(defn
#^{ :doc "Creates a select tag using the given select-options or record info and select-options." }
  select-tag
  ([select-options]
    [:select (:html-options select-options)
      (option-tags (:options select-options))])
  ([record record-name key-name select-options]
    (select-tag
      { :html-options (record-html-options (:html-options select-options) record-name key-name)
        :options (options-select-value (:options select-options) (helpers/h (get record key-name))) })))