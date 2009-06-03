(ns conjure.util.loading-utils
  (:use [clojure.contrib.classpath :as classpath]
        [clojure.contrib.seq-utils :as seq-utils]))

;; Gets the system class loader
(defn system-class-loader []
  (. ClassLoader getSystemClassLoader))

;; Loads a given director and filename using the system class loader and returns the reader for it.
(defn resource-reader [directory filename]
  (let [full-file-path (str directory "/" filename)
        resource (. (system-class-loader) getResourceAsStream full-file-path)]
    (if resource
      (new java.io.InputStreamReader resource)
      (throw (new RuntimeException (str "Cannot find file named: " full-file-path))))))

;; Loads a resource from the class path. Simply pass in the directory and the filename to load.
(defn load-resource [directory filename]
  (load-reader (resource-reader directory filename)))

;; Loads a resource into a string and returns it.
(defn load-resource-as-string [directory filename]
  (let [reader (resource-reader directory filename)
        output (new StringBuffer)]
    (loop [current-char (. reader read)]
      (if (== current-char -1)
        (. output toString)
        (do
          (. output append (char current-char))
          (recur (. reader read)))))))

(defn get-classpath-dir-ending-with [ending]
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith ending))
    (classpath/classpath-directories)))
