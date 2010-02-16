(ns conjure.util.html-utils
  (:import [java.net URLEncoder URLDecoder]
           [java.text SimpleDateFormat]
           [java.util Calendar TimeZone]
           [org.apache.commons.lang StringEscapeUtils])
  (:require [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.logging :as logging]
            [conjure.util.string-utils :as conjure-str-utils]))

(defn
#^{:doc "Url encodes the given string."}
  url-encode [string]
  (. URLEncoder encode string "UTF-8"))

(defn
#^{:doc "Url decodes the given string."}
  url-decode [string]
  (. URLDecoder decode string "UTF-8"))

(defn
#^{:doc "Returns a sequence of keys to use in update-params to get the lowest map to add a value into. See the doc for
update-params for more information on how the key-seq is used."}
  key-seq [full-key-str]
  (let [stripped-key (. full-key-str trim)]
    (if (> (. stripped-key length) 0)
      (if (re-matches #"^([\w-]+)(\[[\w-]+\])*$" stripped-key)
        (cons 
          (keyword (re-find #"^[\w-]+" stripped-key))
          (let [matcher (re-matcher #"\[[\w-]+\]" stripped-key)]
            (for [current-key (repeatedly #(re-find matcher)) :while current-key] 
              (keyword (. current-key substring 1 (- (. current-key length) 1))))))
        (throw (new RuntimeException (str "Key string is not valid: \"" full-key-str "\". Key string must be in the form <name>[<name>]*"))))
      ())))

(defn
#^{:doc "Updates params with the given value placed in a map at the place indicated by the given key sequence.

Examples:
  params = { :foo :bar }, value = :biz
  
  key-seq = [ :baz ] => params = { :foo :bar, :baz :biz }
  key-seq = [ :baz :boz ] => params = { :foo :bar, :baz { :boz :biz } }
  key-seq = [ :baz :boz :buz ] => params = { :foo :bar, :baz { :boz { :buz :biz } } }
  
  params = { :baz { :biz :boz } }, value = :bar
  
  key-seq = [ :baz :foo ] => params = { :baz { :biz :boz, :foo :bar } }"}
  update-params [params key-seq value]
  (if (not-empty key-seq)
    (let [first-key (first key-seq)
          child-key-seq (rest key-seq)]
      (if (and (contains? params first-key) (seq child-key-seq))
        (assoc params first-key (update-params (get params first-key) child-key-seq value))
        (if (not-empty child-key-seq)
          (assoc params first-key (update-params {} child-key-seq value))
          (assoc params first-key value))))
    params))

(defn
#^{:doc "Adds the given query-key-value sequence as a key value pair to the map params."}
  add-param [params query-key-value]
  (update-params params (key-seq (url-decode (first query-key-value))) (url-decode (second query-key-value))))

(defn
#^{:doc "Parses the parameters in the given query-string into a parameter map."}
  parse-query-params [query-string]
  (if query-string
    (reduce add-param {} (filter second (map #(str-utils/re-split #"=" %) (str-utils/re-split #"&" query-string))))
    {}))

(defn-
#^{:doc "Takes a param pair and turns it into a string with the first and second value separated by \"=\". If either 
value is nil, then this function returns nil."}
  str-param-pair [param-pair]
  (let [param-key (first param-pair)
        param-value (second param-pair)]
    (if (and param-key param-value)
      (str (conjure-str-utils/str-keyword param-key) "=" (url-encode (conjure-str-utils/str-keyword param-value))))))

(defn 
#^{ :doc "Converts the given param-map into a url get param string for appending to a url." }
  url-param-str [param-map]
  (if (and param-map (not-empty param-map))
    (str "?" 
      (str-utils/str-join "&"
        (filter identity (map str-param-pair param-map))))))

(defn
#^{ :doc "Adds the given protocal and server to the given url, if it does not already include a protocal and server." }
  full-url [url protocal-and-server]
  (if (. url matches "^\\w+://.+")
    url
    (str protocal-and-server url)))

(defn
#^{ :doc "Returns a string to use as an attribute in an html tag." }
  attribute-str [attribute value]
  (str attribute "=\"" (. StringEscapeUtils escapeHtml value) "\""))

(defn
#^{ :doc "Returns a string of html attributes created from the given attributes map." }
  attribute-list-str [attributes]
  (str-utils/str-join " " 
    (map 
      (fn [key-value-pair] 
        (attribute-str (conjure-str-utils/str-keyword (first key-value-pair)) (second key-value-pair))) 
      attributes)))

(defn
#^{ :doc "Returns the string value of the given date for use in an http cookie." }
  format-cookie-date [date]
  (str 
	  (. (new SimpleDateFormat "EEE, dd-MMM-yyyy HH:mm:ss") 
	  	format 
	  	(let [time-zone (. TimeZone getTimeZone "GMT+0:0")
	  	      gmt-calendar (. Calendar getInstance time-zone)]
		  		(. gmt-calendar setTime date)
		  		(. gmt-calendar getTime)))
	  " GMT"))

(defn
#^{ :doc "Strips quotes from all of the values in the given map." }
  strip-map-value-quotes [map-to-strip]
  (reduce 
    (fn [new-map [key-name value]]
      (assoc new-map key-name (conjure-str-utils/strip-quotes value)))
    {}
    map-to-strip))

(defn
#^{ :doc "Parses a content line and adds it into the given content map. The key for the given line in the content-map is
the name of the attribute." }
  parse-content-line [content-map content-line]
  (if content-line
    (let [colon-index (.indexOf content-line ":")]
      (if (> colon-index 0) 
        (assoc content-map (.substring content-line 0 colon-index)
          (strip-map-value-quotes 
            (conjure-str-utils/str-to-map (.substring content-line (inc colon-index)))))))))

(defn
#^{ :doc "Converts the given multipart form data into a tree of maps." }
  split-multipart-form [string boundary]
  (let [full-boundary (str "--" boundary)]
    (filter 
      (fn [item] 
        (not (or (= item full-boundary) (.startsWith item "--")))) ;(= (.length item) 0)
      (str-utils/re-partition 
        (re-pattern full-boundary) 
        string))))

(declare multipart-form-part)

(defn
#^{ :doc "Parses the data in data-lines and adds the data under the key :data in content-map and returns the result." }
  parse-data [content-map data-lines]
  (let [data (str-utils/str-join "\r\n" data-lines)
        content-type-map (get content-map "Content-Type")]
    (assoc content-map :data 
      (if (or (contains? content-type-map "multipart/mixed") (contains? content-type-map "multipart/form-data")) 
        (map multipart-form-part 
          (drop 1 (split-multipart-form data (get content-type-map "boundary"))))
        data))))

(defn
#^{ :doc "Converts the given multipart form part into a map with keys: :content-disposition, :content-type, and :data.
The values of :content-disposition and :content-type are maps. The value of data is the actual data of a part, If the 
part is also a multipart, then data is a mulipart form map built by multipart-form-data." }
  multipart-form-part [string]
  (let [lines (drop-while empty? (if string (.split string "\\r\\n") []))
        content-lines (take-while #(> (.length %) 0) lines)
        content-map (reduce parse-content-line {} content-lines)] 
    (parse-data content-map (drop (inc (count content-map)) lines))))

(defn
#^{ :doc "Converts the given multipart form data into a tree of maps." }
  multipart-form-data [string boundary]
  (map multipart-form-part 
    (drop 1 (split-multipart-form string boundary))))