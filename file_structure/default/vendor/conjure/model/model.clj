(ns conjure.model.model
  (:import [java.io File]
           [java.sql ResultSet])
  (:require [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as string-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.server.jdbc-connector :as jdbc-connector]
            [clojure.contrib.seq-utils :as seq-utils]))

(defn
#^{:doc "Returns the model name for the given model file."}
  model-from-file [model-file]
  (loading-utils/clj-file-to-symbol-string (. model-file getName)))
  
(defn
#^{:doc "Returns the model namespace for the given model."}
  model-namespace [model]
  (str "models." model))
  
(defn 
#^{:doc "Finds the models directory."}
  find-models-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "models"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
    
(defn
#^{:doc "Returns the model file name for the given model name."}
  model-file-name-string [model-name]
  (str (loading-utils/dashes-to-underscores model-name) ".clj"))
    
(defn
#^{:doc "Creates a new model file from the given model name."}
  create-model-file [models-directory model-name]
  (let [model-file (new File models-directory (model-file-name-string model-name))]
    (if (. model-file exists)
      (println (. model-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating model file" (. model-file getName) "...")
        (. model-file createNewFile)
        model-file))))
        
(defn
#^{:doc "Returns the name of the migration associated with the given model."}
  migration-for-model [model]
  (str "create-" (string-utils/pluralize model)))
  
(defn
#^{:doc "Returns the table name for the given model."}
  model-to-table-name [model]
  (string-utils/pluralize (loading-utils/dashes-to-underscores model)))
  
(defn
#^{:doc "Finds a model file with the given model name."}
  find-model-file [models-directory model-name]
  (file-utils/find-file models-directory (model-file-name-string model-name)))

(defn
#^{:doc "Returns a list of column names from the given result set."}
  results-columns [results]
  (let [meta-data (. results getMetaData)]
      (map (fn [index] (. meta-data getColumnName (inc index))) (range (. meta-data getColumnCount)))))

(defn
#^{:doc "Returns a map of column names to values of the given result set."}
  results-row-map [results]
  (let [columns (results-columns results)]
    (loop [column-name (first columns)
           other-columns (rest columns)
           row-map { }]
      (if (empty? other-columns)
        (assoc row-map (keyword (. column-name toLowerCase)) (. results getString column-name))
        (recur 
          (first other-columns)
          (rest other-columns)
          (assoc row-map
            (keyword (. column-name toLowerCase))
            (. results getString column-name)))))))
            
(defn
#^{:doc "Converts a single row from the given results into a row function from the given row-generator."}
  single-results [row-generator results]
  (row-generator (results-row-map results)))

(defn
#^{:doc "Converts a result set into a list of row functions."}
  read-results [row-generator results]
  (if (. results isLast)
    ()
    (do
      (. results next)
      (cons
        (single-results row-generator results)
        (read-results row-generator results)))))

(defn
#^{:doc "If results is a ResultSet then this method converts the result set into a list of row functions. Otherwise, this method just returns results."}
  convert-results [row-generator results]
  (if (instance? ResultSet results) 
    (read-results row-generator results)
    results))

(defn
  model-row [row-map]
  (fn [op & args]
    (cond
      (get row-map op false) (get row-map op)
      true (throw (new RuntimeException (str "Unknown row operator: " op))))))

(defn
#^{:doc "Creates a connection to the database and returns a function which can be used to query that databse."}
  model-connect [model row-generator]
  (let
    [jdbc-connection (jdbc-connector/connect)
     db-flavor (jdbc-connector/db-flavor)
     table (model-to-table-name model)]
    (fn [op & args]
      (cond
        (= op :execute-query) (convert-results row-generator ((:execute-query db-flavor) jdbc-connection args))
        (= op :execute-update) ((:execute-update db-flavor) jdbc-connection args)
        (= op :find) (convert-results row-generator ((:sql-find db-flavor) jdbc-connection table args))
        (= op :close) (. jdbc-connection close)
        true (throw (new RuntimeException (str "Unknown model operator: " op)))))))

(defmacro
#^{:doc "A macro for the create row function in a model."}
  def-create-row [create-row operation this row-conditions]
  `(defn ~create-row [row-map#] 
      (fn ~this [~operation & args#] 
        (let [row# (model-row row-map#)] 
          (cond
            ~@row-conditions
            true (row# ~operation args#))))))

(defmacro
#^{:doc "A macro for the create row function in a model."}
  def-model-function [model-name create-row-function operation this model-conditions]
  `(defn ~model-name []
      (let [model# (model-connect ~(str model-name) create-row#)]
        (fn ~this [~operation & args#]
            (cond 
              ~@model-conditions
              true (model# ~operation args#))))))
(defmacro
#^{:doc "A macro for defining a model."}
  defmodel
  [model-name operation this model-conditions row-conditions]
  `(do 
    (def-create-row create-row# ~operation ~this ~row-conditions)
    
    (def-model-function ~model-name create-row# ~operation ~this ~model-conditions)))