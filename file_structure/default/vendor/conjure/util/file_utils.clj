(ns conjure.util.file-utils
  (:import [java.io File FileWriter]))

(defn
#^{:doc "Returns the directory where Conjure is running from."}
  user-directory []
  (new File (. (System/getProperties) getProperty "user.dir")))

(defn
#^{:doc "Returns the file object if the given file is in the given directory, nil otherwise."}
  find-file [directory file-name]
  (let [file (new File (. directory getPath) file-name)]
    (if (. file exists)
      file)))
      
(defn
#^{:doc "Returns the file object if the given directory is in the given parent directory, nil otherwise. Simply calls find-file, but this method reads better if you're really looking for a directory."}
  find-directory [parent-directory directory-name]
  (find-file parent-directory directory-name))
  
(defn
#^{:doc "A convience function for simply writing the given content into the file."}
  write-file-content [file content]
  (let [file-writer (new FileWriter file)]
    (. file-writer write content 0 (. content length))
    (. file-writer flush)
    (. file-writer close)))

(defn
#^{:doc "Creates a new child directory under the given base-dir if the child dir does not already exist."}
  create-dir 
  ([base-dir child-dir-name] (create-dir base-dir child-dir-name false))
  ([base-dir child-dir-name silent]
    (let [child-directory (find-directory base-dir child-dir-name)]
      (if child-directory
        (do
          (if (not silent) (println (. child-directory getPath) "directory already exists."))
          child-directory)
        (do
          (if (not silent) (println "Creating" child-dir-name "directory in" (. base-dir getName) "..."))
          (let [child-directory (new File base-dir child-dir-name)]
            (. child-directory mkdirs)
            child-directory))))))

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
        (if (not silent) (println "You must pass in a base directory."))))))

(defn
#^{:doc "Creates and returns the given file if it does not already exist. If it does exist, the method simply prints to
standard out and returns nil"}
  create-file 
  ([file] (create-file file false))
  ([file silent]
    (if (. file exists)
      (if (not silent) (println (. file getName) "already exists. Doing nothing."))
      (do
        (if (not silent) (println "Creating file" (. file getName) "..."))
        (. file createNewFile)
        file))))

(defn
#^{:doc "Deletes the given directory if it contains no files or subdirectories."}
  delete-if-empty [directory]
  (when-not (empty? (file-seq directory))
    (. directory delete)
    true))

(defn
#^{:doc "Deletes if empty all of the given directories in order."}
  delete-all-if-empty [& directories]
  (reduce (fn [deleted directory] (if deleted (delete-if-empty directory))) true directories))