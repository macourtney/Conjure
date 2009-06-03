(ns conjure.util.string-utils
  (:use [clojure.contrib.str-utils :as str-utils]))

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
#^{:doc "If the string's length does not equal total-length then this method returns a new string with length total-length by adding fill-char multiple times to the begining of string. If string's length is already total-length, then this method simply returns it."}
  prefill [string total-length fill-char]
  (if (>= (. string length) total-length)
    string
    (str (str-utils/str-join "" 
      (map
        (fn [index] fill-char) 
        (range (- total-length (. string length))))) string)))