(ns conjure.util.string-utils
  (:import [java.security NoSuchAlgorithmException MessageDigest]
           [java.math BigInteger])
  (:require [clojure.contrib.str-utils :as str-utils]))

(defn
#^{:doc "If the string's length does not equal total-length then this method returns a new string with length 
total-length by adding fill-char multiple times to the beginning of string. If string's length is already total-length,
then this method simply returns it."}
  prefill [string total-length fill-char]
  (let [base-string (if string string "")
        final-length (if total-length total-length 0)]
    (if (>= (. base-string length) final-length)
      base-string
      (str (str-utils/str-join "" 
        (map
          (fn [index] fill-char) 
          (range (- final-length (. base-string length))))) base-string))))
        
(defn
#^{:doc "Converts a keyword to it's string value. Basically, it just removes the ':' from the beginning."}
  str-keyword [incoming-keyword]
  (if (nil? incoming-keyword)
    nil
    (if (keyword? incoming-keyword)
      (. (str incoming-keyword) substring 1)
      (str incoming-keyword))))
    
(defn
#^{:doc "If string ends with the string ending, then remove ending and return the result. Otherwise, return string."}
  strip-ending [string ending]
  (if (and string ending (. string endsWith ending))
    (let [ending-index (- (. string length) (. ending length))]
      (. string substring 0 ending-index))
    string))

(defn
#^{:doc "If the given string does not end with ending, then add ending to it."}
  add-ending-if-absent [string ending]
    (if string
      (if ending
        (if (. string endsWith ending)
          string
          (str string ending))
        string)
      ending))

(defn
#^{ :doc "For the given replace pair, replace all occurences of (first replace-pair) in string with 
(second replace-pair) if (second replace-pair) is not nil." }
  str-replace-pair [string replace-pair]
  (if string
    (let [target (first replace-pair)
          replacement (second replace-pair)]
      (if replacement
        (. string replace (str-keyword target) replacement)
        string))
    nil))

(defn
#^{ :doc "For each key in replace-map, substitute the value in replace map for all occurances of the key in string." }
  str-replace-if 
  ([string replace-map]
    (reduce str-replace-pair string replace-map)))

(defn
#^{ :doc "Replaces any underscores or dashes in the given string to spaces. If string is a keyword, it is converted to a
string before the spaces are added." }
  human-readable [string]
    (if string
      (str-utils/re-gsub #"[_-]" " " (str-keyword string))
      string))

(defn md5-sum
  "Compute the hex MD5 sum of a list of strings."
  [& strings]
  (let [alg (doto (MessageDigest/getInstance "MD5")
              (.reset))]
    (try
      (do
        (doall (map #(. alg update (.getBytes %)) strings))
        (.toString (new BigInteger 1 (.digest alg)) 16))
      (catch NoSuchAlgorithmException e
        (throw (new RuntimeException e))))))

(defn
#^{ :doc "Converts the given string to a map using separator to separate the key value pairs, and equals-separator to 
separate the key from the value." }
  str-to-map 
  ([string] (str-to-map string #";"))
  ([string separator] (str-to-map string separator #"="))
  ([string separator equals-separator]
    (if (and string separator equals-separator)
      (reduce 
        (fn [new-map pair] (assoc new-map (first pair) (second pair)))
        {}
        (map 
          #(filter 
            (fn [equals-seq] (not (re-matches equals-separator equals-seq))) 
            (str-utils/re-partition equals-separator %))
          (filter 
            (fn [equals-pair] (not (re-matches separator equals-pair))) 
            (str-utils/re-partition separator string))))
      nil)))