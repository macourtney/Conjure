(ns conjure.controller.util
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.string-utils :as string-utils]
            [clojure.contrib.ns-utils :as ns-utils]))

(def controllers-dir "controllers")
(def controllers-namespace controllers-dir)
(def controller-file-name-ending "_controller.clj")
(def controller-namespace-ending "-controller")

(defn 
#^{ :doc "Finds the controller directory." }
  find-controllers-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith controllers-dir))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))

(defn
#^{ :doc "Returns the controller file name for the given controller name." }
  controller-file-name-string [controller-name]
  (if (and controller-name (> (. controller-name length) 0))
    (str (loading-utils/dashes-to-underscores controller-name) controller-file-name-ending)))

(defn
#^{ :doc "Returns the controller file name generated from the given request map." }
  controller-file-name [request-map]
  (controller-file-name-string (:controller request-map)))
  
(defn
#^{ :doc "Returns the controller name for the given controller file." }
  controller-from-file [controller-file]
  (if controller-file
    (let [file-to-symbol (loading-utils/clj-file-to-symbol-string (. controller-file getName))]
      (string-utils/strip-ending file-to-symbol controller-namespace-ending))))

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
    (str controllers-namespace "." (loading-utils/underscores-to-dashes controller) controller-namespace-ending)))
    
(defn
#^{ :doc "Returns the names of all of the controllers for this app." }
  all-controllers []
  (map controller-from-file 
    (filter 
      #(. (. % getName) endsWith controller-file-name-ending) 
      (. (find-controllers-directory) listFiles))))

(defn
#^{ :doc "Returns true if the given controller exists." }
  controller-exists? [controller-file-name]
  (loading-utils/resource-exists? (str controllers-dir "/" controller-file-name)))

(defn
#^{ :doc "Loads the given controller file." }
  load-controller [controller-filename]
  (loading-utils/load-resource controllers-dir controller-filename))

(defn
#^{ :doc "Returns fully qualified action generated from the given request map." }
  fully-qualified-action [request-map]
  (if request-map
    (let [controller (:controller request-map)
          action (:action request-map)]
      (if (and controller action)
        (str (controller-namespace controller) "/" action)))))

(defn
#^{ :doc "Calls the given controller with the given request map returning the response." }
  call-controller [request-map]
  (let [controller-file (controller-file-name request-map)]
    (when (and controller-file (controller-exists? controller-file))
      (let [action (fully-qualified-action request-map)]
        (logging/debug (str "Running action: " action))
        (load-controller controller-file)
        (if (ns-resolve 
              (ns-utils/get-ns (symbol (controller-namespace (:controller request-map))))
              (symbol (:action request-map)))
          ((load-string action) request-map))))))