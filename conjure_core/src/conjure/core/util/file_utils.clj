(ns conjure.core.util.file-utils
  (:import [java.io File FileWriter]
           [java.security AccessControlException])
  (:require [clojure.contrib.duck-streams :as duck-streams]
            [clojure.contrib.logging :as logging]))

(defn
#^{:doc "Returns the directory where Conjure is running from."}
  user-directory []
  (new File (. (System/getProperties) getProperty "user.dir")))

(defn
#^{:doc "Returns the file object if the given file is in the given directory, nil otherwise."}
  find-file [directory file-name]
  (when (and file-name directory (string?  file-name) (instance? File directory))
    (let [file (File. (.getPath directory) file-name)]
      (when (and file (.exists file))
        file))))

(defn
#^{:doc "Returns the file object if the given directory is in the given parent directory, nil otherwise. Simply calls find-file, but this method reads better if you're really looking for a directory."}
  find-directory [parent-directory directory-name]
  (find-file parent-directory directory-name))
  
(defn
#^{:doc "A convience function for simply writing the given content into the file."}
  write-file-content [file content]
  (when (and file content (.exists (.getParentFile file)))
    (duck-streams/spit file content)))

(defn
#^{:doc "Creates a new child directory under the given base-dir if the child dir does not already exist."}
  create-dir 
  ([base-dir child-dir-name] (create-dir base-dir child-dir-name false))
  ([base-dir child-dir-name silent]
    (if-let [child-directory (find-directory base-dir child-dir-name)]
      child-directory
      (do
        (logging/info (str "Creating " child-dir-name " directory in " (. base-dir getName) "..."))
        (let [child-directory (new File base-dir child-dir-name)]
          (.mkdirs child-directory)
          child-directory)))))

(defn
#^{:doc "Recursively creates the given child-dirs under the given base-dir.

For example: (create-dirs (new File \"foo\") \"bar\" \"baz\") creates the directory /foo/bar/baz

Note: this method prints a bunch of stuff to standard out."}
  create-dirs 
  ([dirs] (create-dirs dirs false))
  ([dirs silent]
    (let [base-dir (first dirs)
          child-dirs (rest dirs)]
      (if base-dir
        (reduce (fn [base-dir child-dir] (create-dir base-dir child-dir silent)) base-dir child-dirs)
        (logging/error "You must pass in a base directory.")))))

(defn
#^{ :doc "Creates and returns the given file if it does not already exist. If it does exist, the method simply prints to
standard out and returns nil" }
  create-file 
  ([file] (create-file file false))
  ([file silent]
    (if (. file exists)
      (logging/info (str (. file getName) " already exists. Doing nothing."))
      (do
        (logging/info (str "Creating file " (. file getName) "..."))
        (. file createNewFile)
        file))))

(defn
#^{ :doc "Deletes the given directory if it contains no files or subdirectories." }
  delete-if-empty [directory]
  (when-not (empty? (file-seq directory))
    (. directory delete)
    true))

(defn
#^{ :doc "Deletes if empty all of the given directories in order." }
  delete-all-if-empty [& directories]
  (reduce (fn [deleted directory] (if deleted (delete-if-empty directory))) true directories))

(defn
#^{ :doc "Deletes the given directory even if it contains files or subdirectories. This function will attempt to delete
all of the files and directories in the given directory first, before deleting the directory. If the directory cannot be
deleted, this function aborts and returns nil. If the delete finishes successfully, then this function returns true." }
  recursive-delete [directory]
  (if (.isDirectory directory) 
    (when (reduce #(and %1 (recursive-delete %2)) true (.listFiles directory))
      (.delete directory))
    (.delete directory)))

(defn
#^{ :doc "Returns true if the given file is a directory. False otherwise, even if the is directory check causes an
AccessControlException which may happen when running in Google App Engine." }
  is-directory? [file]
  (try
    (.isDirectory file)
    (catch AccessControlException access-control-exception
      false)))