(ns conjure.util.string-utils
  (:require [clojure.contrib.str-utils :as str-utils]
            [clj-record.util :as clj-record-util]))

(defn 
#^{:doc "Chops the given string into smaller strings based on the delimiter and returns the result in a sequence."}
  tokenize [string delimiter]
  (let [tokenizer (new java.util.StringTokenizer string delimiter)]
    (if (. tokenizer hasMoreTokens)
      (loop [out (cons (. tokenizer nextToken) ())]
        (if (. tokenizer hasMoreTokens)
          (recur (cons (. tokenizer nextToken) out))
          (reverse out)))
      nil)))
      
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
  str-keyword [keyword]
  (if (keyword? keyword)
    (. (str keyword) substring 1)
    (str keyword)))
    
(defn
#^{:doc "If string ends with the string ending, then remove ending and return the result. Otherwise, return string."}
  strip-ending [string ending]
  (if (and string ending (. string endsWith ending))
    (let [ending-index (- (. string length) (. ending length))]
      (. string substring 0 ending-index))
    string))
    
(defn
#^{:doc "Pluralizes the given word. The current version of this function just adds an 's' to the end of the string. Eventually, this method should become more robust."}
  pluralize [string]
  (clj-record-util/pluralize string))