(ns conjure.controller.controller
  (:import [java.io File])
  (:require [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]
            [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.string-utils :as string-utils]))

(defn 
#^{:doc "Finds the controller directory."}
  find-controllers-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "controllers"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))

(defn
#^{:doc "Returns the controller file name for the given controller name."}
  controler-file-name-string [controller-name]
  (str (loading-utils/dashes-to-underscores controller-name) "_controller.clj"))
  
(defn
#^{:doc "Returns the controller name for the given controller file."}
  controler-from-file [controller-file]
  (let [file-to-symbol (loading-utils/clj-file-to-symbol-string (. controller-file getName))]
    (string-utils/strip-ending file-to-symbol "-controller")))

(defn
#^{:doc "Finds a controller file with the given controller name."}
  find-controller-file [controllers-directory controller-name]
  (file-utils/find-file controllers-directory (controler-file-name-string controller-name)))
      
(defn
#^{:doc "Creates a new controller file from the given controller name."}
  create-controller-file [controllers-directory controller-name]
  (let [controller-file (new File controllers-directory (controler-file-name-string controller-name))]
    (if (. controller-file exists)
      (println (. controller-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating controller file" (. controller-file getName) "...")
        (. controller-file createNewFile)
        controller-file))))
    
(defn
#^{:doc "Returns the controller namespace for the given controller."}
  controller-namespace [controller]
  (str "controllers." controller "-controller"))