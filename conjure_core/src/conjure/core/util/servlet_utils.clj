(ns conjure.core.util.servlet-utils
  (:import [java.io File FileInputStream])
  (:require [clojure.contrib.logging :as logging] 
            [conjure.core.server.request :as request]
            [conjure.core.util.file-utils :as file-utils]
            [conjure.core.util.loading-utils :as loading-utils]))

(defn
  servlet-uri-path [servlet-context]
  (when servlet-context
    (.getName (File. (.getRealPath servlet-context "/"))))) 

(defn
  servlet-uri? [servlet-context uri]
  (when-let [servlet-uri-path (servlet-uri-path servlet-context)]
    (or (= uri (str "/" servlet-uri-path)) (.startsWith uri (str "/" servlet-uri-path "/")))))

(defn
  servlet-sub-path [servlet-context uri]
  (let [servlet-uri-path (servlet-uri-path servlet-context)]
    (if (= uri (str "/" servlet-uri-path))
      "/" 
      (.substring uri (inc (count servlet-uri-path))))))

(defn
  convert-servlet-path [servlet-context uri]
  (if (servlet-uri? servlet-context uri)
    (servlet-sub-path servlet-context uri)
    uri)) 

(defn
  real-path [servlet-context relative-path]
  (when (and servlet-context relative-path)
    (let [servlet-path (convert-servlet-path servlet-context relative-path)]
      (.getRealPath servlet-context (str "WEB-INF/classes/" servlet-path))))) 

(defn
  find-file [servlet-context relative-path]
  (when-let [file-path (real-path servlet-context relative-path)]
    (let [servlet-file (File. file-path)]
      (when (.exists servlet-file)
        servlet-file)))) 

(defn
  find-servlet-resource [servlet-context relative-path]
  (when-let [resource-file (find-file servlet-context relative-path)]
    (when (.isFile resource-file)
      (FileInputStream. resource-file))))

(defn
  servlet [request-map]
  (or (:servlet request-map) (:servlet (:request request-map)))) 

(defn
  servlet-context [request-map]
  (if (map? request-map)
    (or
      (:servlet-context request-map)
      (:servlet-context (:request request-map)))
    request-map))

(defn
  find-resource [servlet-context-or-map relative-path]
  (if-let [body (loading-utils/find-resource relative-path)]
    body
    (if-let [servlet-context (servlet-context servlet-context-or-map)]
      (find-servlet-resource servlet-context relative-path)
      (when-let [resource-file (File. (file-utils/user-directory) relative-path)]
        (when (.exists resource-file)
          (FileInputStream. resource-file))))))

(defn
  find-servlet-directory 
  ([directory-name] (find-servlet-directory directory-name (request/servlet-context))) 
  ([directory-name servlet-context]
    (when-let [servlet-directory (find-file servlet-context directory-name)]
      (logging/debug (str "servlet-directory: " servlet-directory))
      servlet-directory)))

(defn
  all-files 
  ([directory-name] (all-files directory-name (request/servlet-context)))
  ([directory-name servlet-context]
    (when-let [directory (find-file servlet-context directory-name)]
      (.listFiles directory))))

(defn
  all-file-names
  ([directory-name] (all-file-names directory-name (request/servlet-context)))
  ([directory-name servlet-context]
    (when-let [all-files (all-files directory-name servlet-context)]
      (map #(.getName %) all-files))))

(defn
  add-servlet-path [servlet-context original-uri uri-path]
  (if (servlet-uri? servlet-context original-uri)
    (str "/" (servlet-uri-path servlet-context) uri-path) 
    uri-path))