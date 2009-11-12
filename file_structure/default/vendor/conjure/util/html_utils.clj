(ns conjure.util.html-utils
  (:import [java.net URLEncoder URLDecoder])
  (:require [clojure.contrib.str-utils :as str-utils]
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
  (update-params params (key-seq (first query-key-value)) (url-decode (second query-key-value))))

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