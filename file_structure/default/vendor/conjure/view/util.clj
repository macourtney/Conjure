(ns conjure.view.util
  (:require [conjure.util.file-utils :as file-utils]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.session-utils :as session-utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [clojure.contrib.logging :as logging]
            [clojure.contrib.ns-utils :as ns-utils]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as str-utils]
            environment
            session-config))

(def loaded-views (atom {}))

(defn 
#^{ :doc "Finds the views directory which contains all of the files which describe the html pages of the app." }
  find-views-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "views"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))

(defn
#^{ :doc "Returns all of the view files in all of the directories in the view directory." }
  view-files []
  (filter loading-utils/clj-file? (file-seq (find-views-directory))))

(defn
#^{ :doc "Finds a controller directory for the given controller in the given view directory." }
  find-controller-directory 
  ([controller] (find-controller-directory (find-views-directory) controller))
  ([view-directory controller]
    (if controller
      (file-utils/find-directory view-directory (loading-utils/dashes-to-underscores controller)))))
  
(defn
#^{ :doc "Finds a view file with the given controller-directory and action." }
  find-view-file [controller-directory action]
  (if (and controller-directory action)
    (file-utils/find-file controller-directory (loading-utils/symbol-string-to-clj-file action))))

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
#^{ :doc "Returns the view namespace for the given view file." }
  view-namespace 
  [view-file]
  (loading-utils/file-namespace (.getParentFile (find-views-directory)) view-file))

(defn
#^{ :doc "Returns a sequence of all view namespaces." }
  all-view-namespaces []
  (map #(symbol (view-namespace %)) (view-files)))

(defn
#^{ :doc "Adds the given action to the given loaded action set." }
  add-loaded-action [loaded-action-set action]
  (let [action-key (keyword action)]
    (if loaded-action-set
      (if (not (contains? loaded-action-set action-key))
        (conj loaded-action-set action-key)
        loaded-action-set)
      #{ action-key })))

(defn
#^{ :doc "Adds the given controller and action to the given loaded views map." }
  assoc-loaded-views [loaded-view-map controller action]
  (let [controller-key (keyword controller)]
    (assoc loaded-view-map controller-key 
      (add-loaded-action (get loaded-view-map controller-key) action))))

(defn
#^{ :doc "Loads the view corresponding to the values in the given request map." }
  load-view [{ controller :controller, action :action, :as request-map }]
  (let [view-namespace (request-view-namespace request-map)]
    (require :reload (symbol view-namespace))
    (reset! loaded-views (assoc-loaded-views @loaded-views controller action))
    (loading-utils/reload-conjure-namespaces view-namespace)))

(defn
#^{ :doc "Returns true if the view corresponding to the given request-map is already loaded." }
  view-loaded? [{ controller :controller, action :action }]
  (get (get @loaded-views controller) action))

(defn
#^{ :doc "Returns the rendered view from the given request-map." }
  render-view [request-map & params]
  (when (or environment/reload-files (not (view-loaded? request-map)))
    (load-view request-map))
  (let [view-namespace (request-view-namespace request-map)]
    (apply
      (ns-resolve (ns-utils/get-ns (symbol view-namespace)) (symbol "render-view"))
      request-map params)))

(defn-
#^{ :doc "Creates a new request-map for use when rendering a layout. The new map is similar to the given request-map 
except the controller is \"layouts\", the action is layout-name, and layout-info contains the controller and action from
the given request-map." }
  merge-layout-request-map [request-map layout-name]
  (merge request-map 
    { :controller "layouts", 
      :action layout-name 
      :layout-info 
        (merge
          (:layout-info request-map) 
          (select-keys request-map [:controller :action :params])) }))

(defn
#^{:doc "Returns the rendered layout for the given layout name."}
  render-layout [layout-name request-map body]
  (if layout-name
    (let [body-is-map? (map? body)
          layout-body (if body-is-map? (:body body) body)
          layout-request-map (merge-layout-request-map request-map layout-name)
          full-body (render-view layout-request-map layout-body)]
      (if body-is-map?
        (assoc body :body full-body)
        full-body))
    body))

(defn
#^{:doc "Returns the full host string from the given params. Used by url-for." }
  full-host [request-map]
  (let [request (:request request-map)
        server-name (or (:server-name request-map) (:server-name request))
        scheme (or (:scheme request-map) (:scheme request))
        user (:user request-map)
        password (:password request-map)
        port (:port request-map)
        server-port (:server-port request)]
    (if (and server-name (not (:only-path request-map)))
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
  id-from [request-map]
  (let [id (:id (:params request-map))]
    (if (and id (map? id))
      (:id id)
      id)))
      
(defn-
#^{ :doc "Returns the value of :anchor from the given parameters and adds a '#' before it. If the key :anchor does not 
exist in params, then this method returns nil This method is used by url-for to get the id from from the params passed 
to it." }
  anchor-from [request-map]
  (let [anchor (:anchor request-map)]
    (if anchor
      (str "#" anchor))))

(defn
#^{ :doc "Returns the params merged with the request-map. Only including the keys from request-map used by url-for" }
  merge-url-for-params [request-map params]
    (merge 
      (select-keys 
        request-map 
        [:controller :action :request])
      params))

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
  ([request-map]
  (let [controller (conjure-str-utils/str-keyword (:controller request-map))
        action (conjure-str-utils/str-keyword (:action request-map))
        url-params (or (dissoc (:params request-map) :id) {})
        session-id (session-utils/session-id request-map)]
    (if (and controller action)
      (apply str 
        (seq-utils/flatten
          [ (full-host request-map) 
            (interleave 
              (repeat "/") 
              (filter #(not (nil? %))
                [(loading-utils/dashes-to-underscores controller) (loading-utils/dashes-to-underscores action) (id-from request-map) (anchor-from request-map)]))
            (let [new-session-id 
                    (if (not session-config/use-session-cookie) (or session-id (session-utils/create-session-id)))
                  new-url-params 
                    (if new-session-id (assoc url-params :session-id new-session-id) url-params)]
              (if (seq new-url-params)
                (html-utils/url-param-str new-url-params)))]))
      (throw (new RuntimeException (str "You must pass a controller and action to url-for. " request-map)))))))