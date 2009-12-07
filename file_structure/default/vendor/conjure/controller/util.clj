(ns conjure.controller.util
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.string-utils :as string-utils]))

(defn 
#^{ :doc "Finds the controller directory." }
  find-controllers-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "controllers"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))

(defn
#^{ :doc "Returns the controller file name for the given controller name." }
  controller-file-name-string [controller-name]
  (if (and controller-name (> (. controller-name length) 0))
    (str (loading-utils/dashes-to-underscores controller-name) "_controller.clj")))
  
(defn
#^{ :doc "Returns the controller name for the given controller file." }
  controller-from-file [controller-file]
  (if controller-file
    (let [file-to-symbol (loading-utils/clj-file-to-symbol-string (. controller-file getName))]
      (string-utils/strip-ending file-to-symbol "-controller"))))

(defn
#^{ :doc "Finds a controller file with the given controller name." }
  find-controller-file
  ([controller-name] (find-controller-file (find-controllers-directory) controller-name)) 
  ([controllers-directory controller-name]
    (if controller-name
      (file-utils/find-file controllers-directory (controller-file-name-string controller-name)))))
  
(defn
#^{ :doc "Returns the controller namespace for the given controller." }
  controller-namespace [controller]
  (if controller
    (str "controllers." (loading-utils/underscores-to-dashes controller) "-controller")))
    
(defn
#^{ :doc "Returns the names of all of the controllers for this app." }
  all-controllers []
  (map controller-from-file 
    (filter 
      #(. (. % getName) endsWith "_controller.clj") 
      (. (find-controllers-directory) listFiles))))