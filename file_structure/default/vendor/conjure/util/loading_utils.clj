(ns conjure.util.loading-utils
  (:import [java.io File])
  (:require [clojure.contrib.classpath :as classpath]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as clojure-str-utils]
            [clojure.contrib.java-utils :as java-utils]
            [conjure.util.string-utils :as string-utils]))

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
  (let [reader (resource-reader directory filename)]
    (try
      (load-reader reader)
      (catch Exception exception
        (throw (RuntimeException. (str "An error occured while reading file: " directory "/" filename) exception)))
      (finally (. reader close)))))

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
#^{:doc "Converts the given input stream into a lazy sequence of bytes."}
  seq-input-stream
  ([input-stream] (map byte (take-while #(>= % 0) (repeatedly #(. input-stream read)))))
  ([input-stream length] (map byte (take length (repeatedly #(. input-stream read))))))

(defn
#^{:doc "Converts an input stream to a byte array."}
  byte-array-input-stream 
  ([input-stream] (into-array Byte/TYPE (seq-input-stream input-stream)))
  ([input-stream length] (into-array Byte/TYPE (seq-input-stream input-stream length))))

(defn
#^{:doc "Converts an input stream to a string."}
  string-input-stream 
  ([input-stream] (new String (byte-array-input-stream input-stream)))
  ([input-stream length] (new String (byte-array-input-stream input-stream length))))

(defn 
#^{:doc "Gets the dir from the class path which ends with the given ending"}
  get-classpath-dir-ending-with [ending]
  (seq-utils/find-first 
    (fn [directory] (. (. directory getPath) endsWith ending))
    (classpath/classpath-directories)))
    
(defn
#^{:doc "Converts all dashes to underscores in string."}
  dashes-to-underscores [string]
  (if string
    (clojure-str-utils/re-gsub #"-" "_" string)
    string))
    
(defn
#^{:doc "Converts all underscores to dashes in string."}
  underscores-to-dashes [string]
  (if string
    (clojure-str-utils/re-gsub #"_" "-" string)
    string))
  
(defn
#^{:doc "Returns the file separator used on this system."}
  file-separator []
  (java-utils/get-system-property "file.separator"))
  
(defn
#^{:doc "Converts all slashes to periods in string."}
  slashes-to-dots [string]
  (if string
    (clojure-str-utils/re-gsub #"/|\\" "." string) ; "\" Fixing a bug with syntax highlighting
    string))
    
(defn
#^{:doc "Converts all periods to slashes in string."}
  dots-to-slashes [string]
  (if string
    (. string replace "." (file-separator))
    string))

(defn
#^{:doc "Converts the given clj file name to a symbol string. For example: \"loading_utils.clj\" would get converted into \"loading-utils\""}
  clj-file-to-symbol-string [file-name]
  (slashes-to-dots (underscores-to-dashes (string-utils/strip-ending file-name ".clj"))))
  
(defn
#^{:doc "Converts the given symbol string to a clj file name. For example: \"loading-utils\" would get converted into \"loading_utils.clj\""}
  symbol-string-to-clj-file [symbol-name]
  (let [dashed-name (dashes-to-underscores symbol-name)]
    (if (and dashed-name (> (. dashed-name length) 0))
      (str (dots-to-slashes dashed-name) ".clj")
      dashed-name)))

(defn
#^{:doc "Returns a string for the namespace of the given file in the given directory."}
  namespace-string-for-file [directory file-name]
  (if file-name
    (if (and directory (> (. (. directory trim) length) 0))
      (let [trimmed-directory (. directory trim)
            slash-trimmed-directory (if (or (. trimmed-directory startsWith "/") (. trimmed-directory startsWith "\\")) (. trimmed-directory substring 1) trimmed-directory)]
        (str (slashes-to-dots (underscores-to-dashes slash-trimmed-directory)) "." (clj-file-to-symbol-string file-name)))
      (clj-file-to-symbol-string file-name))
    file-name))