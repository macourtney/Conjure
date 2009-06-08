(ns conjure.util.file-utils
  (:import [java.io File]))

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