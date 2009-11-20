(ns conjure.view.util
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defn 
#^{:doc "Finds the views directory which contains all of the files which describe the html pages of the app."}
  find-views-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "views"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
  
(defn
#^{:doc "Finds a controller directory for the given controller in the given view directory."}
  find-controller-directory 
  ([controller] (find-controller-directory (find-views-directory) controller))
  ([view-directory controller]
    (if controller
      (file-utils/find-directory view-directory (loading-utils/dashes-to-underscores controller)))))
  
(defn
#^{:doc "Finds a view file with the given controller-directory and action."}
  find-view-file [controller-directory action]
  (if (and controller-directory action)
    (file-utils/find-file controller-directory (loading-utils/symbol-string-to-clj-file action))))
  
(defn
#^{:doc "Loads the view corresponding to the values in the given request map."}
  load-view [request-map]
  (loading-utils/load-resource 
    (str "views/" (loading-utils/dashes-to-underscores (:controller request-map))) 
    (str (loading-utils/dashes-to-underscores (:action request-map)) ".clj")))

(defn
#^{:doc "Returns the view namespace request map."}
  request-view-namespace [request-map]
  (if request-map
    (str "views." (loading-utils/underscores-to-dashes (:controller request-map)) "." 
      (loading-utils/underscores-to-dashes (conjure-str-utils/str-keyword (:action request-map))))))

(defn
#^{:doc "Returns the view namespace for the given controller and action."}
  view-namespace-by-action [controller action]
  (if (and controller action)
    (request-view-namespace 
      { :controller controller 
        :action action })))

(defn
#^{:doc "Returns the view namespace for the given view file."}
  view-namespace [controller view-file]
  (if (and controller view-file)
    (view-namespace-by-action controller (loading-utils/clj-file-to-symbol-string (. view-file getName)))))

(defn
#^{:doc "Returns the rendered view from the given request-map."}
  render-view [request-map & params]
  (load-view request-map)
  (apply
    (eval (read-string (str (request-view-namespace request-map) "/render-view")))
    request-map params))

(defn
#^{:doc "Returns the rendered layout for the given layout name."}
  render-layout [layout-name request-map body]
  (let [layouts-request-map (assoc request-map :controller "layouts")
        application-request-map (assoc layouts-request-map :action (if layout-name layout-name "application"))]
    (render-view application-request-map body)))

(defn
#^{:doc "Returns the full host string from the given params. Used by url-for." }
  full-host [params]
  (let [server-name (:server-name params)
        scheme (:scheme params)
        user (:user params)
        password (:password params)
        port (:port params)
        server-port (:server-port params)]
    (if (and server-name (not (:only-path params)))
      (str 
        (if scheme (conjure-str-utils/str-keyword scheme) "http") "://" 
        (if (and user password) (str user ":" password "@")) 
        server-name 
        (if port 
          (str ":" port)
          (if (and server-port (not (= server-port 80))) 
            (str ":" server-port)))))))
            
(defn
#^{:doc "Returns the value of :id from the given parameters. If the value of :id is a map, then this method returns the
value of :id in the map. This method is used by url-for to get the id from from the params passed to it."}
  id-from [params]
  (let [id (:id params)]
    (if (and id (map? id))
      (:id id)
      id)))
      
(defn-
#^{:doc "Returns the value of :anchor from the given parameters and adds a '#' before it. If the key :anchor does not 
exist in params, then this method returns nil This method is used by url-for to get the id from from the params passed 
to it."}
  anchor-from [params]
  (let [anchor (:anchor params)]
    (if anchor
      (str "#" anchor))))

(defn
#^{:doc "Returns the params merged with the request-map. Only including the keys from request-map used by url-for"}
  merge-url-for-params [request-map params]
  (merge (select-keys request-map [:controller :action :scheme :request-method :server-name :server-port ]) params))

(defn
#^{:doc 
"Returns the url for the given parameters. The following parameters are valid:

     :action - The name of the action to link to.
     :controller - The name of the controller to link to.
     :id - The id to pass, or if id links to a map, then the value of :id in that map is used. (Optional)
     :anchor - Specifies the anchor name to be appended to the path.
     :user - Inline HTTP authentication (only used if :password is also present)
     :password - Inline HTTP authentication (only use if :user is also present)
     :scheme - Overrides the default scheme. Example values: :http, :ftp
     :server-name - Overrides the default server name.
     :port - Overrides the default server port."}
  url-for
  ([request-map params] (url-for (merge-url-for-params request-map params))) 
  ([params]
  (let [controller (conjure-str-utils/str-keyword (:controller params))
        action (conjure-str-utils/str-keyword (:action params))]
    (if (and controller action)
      (apply str 
        (full-host params) 
        (interleave 
          (repeat "/") 
          (filter #(not (nil? %))
            [controller action (id-from params) (anchor-from params)])))
      (throw (new RuntimeException (str "You must pass a controller and action to url-for. " params)))))))