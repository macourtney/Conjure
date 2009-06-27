(ns conjure.util.loading-utils
  (:use [clojure.contrib.classpath :as classpath]
        [clojure.contrib.seq-utils :as seq-utils]
        [conjure.util.string-utils :as string-utils]
        [clojure.contrib.str-utils :as clojure-str-utils]))

(defn
#^{:doc "Gets the system class loader"}
  system-class-loader []
  (. ClassLoader getSystemClassLoader))

(defn
#^{:doc "Loads a given director and filename using the system class loader and returns the reader for it."}
  resource-reader [directory filename]
  (let [full-file-path (str directory "/" filename)
        resource (. (system-class-loader) getResourceAsStream full-file-path)]
    (if resource
      (new java.io.InputStreamReader resource)
      (throw (new RuntimeException (str "Cannot find file named: " full-file-path))))))

(defn
#^{:doc "Loads a resource from the class path. Simply pass in the directory and the filename to load."}
  load-resource [directory filename]
  (load-reader (resource-reader directory filename)))

(defn 
#^{:doc "Loads a resource into a string and returns it."}
  load-resource-as-string [directory filename]
  (let [reader (resource-reader directory filename)
        output (new StringBuffer)]
    (loop [current-char (. reader read)]
      (if (== current-char -1)
        (. output toString)
        (do
          (. output append (char current-char))
          (recur (. reader read)))))))

(defn 
#^{:doc "Gets the dir from the class path which ends with the given ending"}
  get-classpath-dir-ending-with [ending]
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith ending))
    (classpath/classpath-directories)))
    
(defn
#^{:doc "Converts all dashes to underscores in string."}
  dashes-to-underscores [string]
  (if string
    (re-gsub #"-" "_" string)
    string))
    
(defn
#^{:doc "Converts all underscores to dashes in string."}
  underscores-to-dashes [string]
  (if string
    (re-gsub #"_" "-" string)
    string))
  
(defn
#^{:doc "Converts all slashes to periods in string."}
  slashes-to-dots [string]
  (if string
    (re-gsub #"/|\\" "." string)
    string))

(defn
#^{:doc "Converts the given clj file name to a symbol string. For example: \"loading_utils.clj\" would get converted into \"loading-utils\""}
  clj-file-to-symbol-string [file-name]
  (underscores-to-dashes (string-utils/strip-ending file-name ".clj")))
  
(defn
#^{:doc "Converts the given symbol string to a clj file name. For example: \"loading-utils\" would get converted into \"loading_utils.clj\""}
  symbol-string-to-clj-file [symbol-name]
  (str (dashes-to-underscores symbol-name) ".clj"))

(defn
#^{:doc "Returns a string for the namespace of the given file in the given directory."}
  namespace-string-for-file [directory file-name]
  (str (slashes-to-dots (underscores-to-dashes directory)) "." (clj-file-to-symbol-string file-name)))