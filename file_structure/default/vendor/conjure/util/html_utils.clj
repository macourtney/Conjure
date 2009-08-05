(ns conjure.util.html-utils
  (:import [java.net URLEncoder URLDecoder])
  (:require [clojure.contrib.str-utils :as str-utils]))

(defn
#^{:doc "Url encodes the given string."}
  url-encode [string]
  (. URLEncoder encode string "UTF-8"))

(defn
#^{:doc "Url decodes the given string."}
  url-decode [string]
  (. URLDecoder decode string "UTF-8"))

(defn
#^{:doc "Adds the given query-key-value sequence as a key value pair to the map params."}
  add-param [params query-key-value]
  (assoc params (keyword (first query-key-value)) (url-decode (second query-key-value))))

(defn
#^{:doc "Parses the parameters in the given query-string into a parameter map."}
  parse-query-params [query-string]
  (if query-string
    (reduce add-param {} (filter second (map #(str-utils/re-split #"=" %) (str-utils/re-split #"&" query-string))))
    {}))