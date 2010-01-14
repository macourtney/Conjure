(ns conjure.util.javascript-utils
  (require [clojure.contrib.str-utils :as str-utils]))

(defn
#^{ :doc "Generates a java script function string with the given parameters and body." }
  function [params & body]
  (str "function(" (str-utils/str-join ", " params) "){" (apply str body) "}"))