(ns conjure.util.map-utils
  (:require [clojure.contrib.logging :as logging]))

(defn
#^{ :doc "Works like assoc, but only associates if condition is true." }
  assoc-if 
  ([condition map key val] 
    (if condition
      (assoc map key val)
      map))
  ([condition map key val & kvs]
    (reduce 
      (fn [output key-pair] 
        (assoc-if condition output (first key-pair) (second key-pair)))
      (assoc-if condition map key val)
      (partition 2 kvs))))

(defn
#^{ :doc "Returns a new map only including keys from the given map where pred
returns true. Pred must be a function which takes a key and value from map." }
  filter-map [pred map]
  (reduce 
    (fn [output key-pair] 
      (assoc-if (apply pred key-pair) 
        output (first key-pair) (second key-pair)))
    {} map))

(defn
#^{ :doc "Returns a new map with all keys of value nil dropped." }
  drop-nils [map]
  (filter-map #(identity %2) map))