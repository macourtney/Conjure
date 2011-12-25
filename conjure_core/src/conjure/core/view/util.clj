(ns conjure.core.view.util
  (:require [clojure.tools.logging :as logging]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.html-utils :as html-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.servlet-utils :as servlet-utils]
            [clojure.tools.string-utils :as conjure-str-utils]
            [config.session-config :as session-config]
            [conjure.core.config.environment :as environment]
            [conjure.core.server.request :as request]
            [conjure.core.util.conjure-utils :as conjure-utils]
            [conjure.core.util.session-utils :as session-utils]))

(def views-dir "views")

(def loaded-views (atom {}))

(defn 
#^{ :doc "Finds the views directory which contains all of the files which describe the html pages of the app." }
  find-views-directory []
  (environment/find-in-source-dir views-dir))

(defn
#^{ :doc "Returns all of the view files in all of the directories in the view directory." }
  view-files []
  (filter loading-utils/clj-file? 
    (let [views-directory (find-views-directory)]
      (when views-directory
        (file-seq views-directory)))))

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
  request-view-namespace 
  ([] (request-view-namespace (request/controller) (request/action)))
  ([controller action]
  (when (and controller action)
    (str "views." (loading-utils/underscores-to-dashes controller) "." 
      (loading-utils/underscores-to-dashes (conjure-str-utils/str-keyword action))))))

(defn
#^{:doc "Returns the view namespace for the given controller and action."}
  view-namespace-by-action [controller action]
  (when (and controller action)
    (request-view-namespace controller action)))

(defn
#^{ :doc "Returns the view namespace for the given view file." }
  view-namespace 
  [view-file]
  (when-let [views-directory (find-views-directory)]
    (loading-utils/file-namespace (.getParentFile views-directory) view-file)))

(defn
#^{ :doc "Returns a sequence of all view namespaces." }
  all-view-namespaces []
  (filter #(.startsWith (name (ns-name %)) "views.") (all-ns)))

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
  load-view 
  ([] (load-view (request/controller) (request/action)))
  ([controller action]
    (let [view-namespace (request-view-namespace controller action)]
      (require :reload (symbol view-namespace))
      (reset! loaded-views (assoc-loaded-views @loaded-views controller action))
      (conjure-utils/reload-conjure-namespaces view-namespace))))

(defn
#^{ :doc "Returns true if the view corresponding to the request-map or given controller and action is already loaded." }
  view-loaded?
  ([] (view-loaded? (request/controller) (request/action)))
  ([controller action]
    (get (get @loaded-views controller) action)))

(defn
  #^{ :doc "Returns the namespace of the view for the request map. The namespace is an actual namespace object." }
  get-view-ns []
  (find-ns (symbol (request-view-namespace))))

(defn
  #^{ :doc "Calls the render function with the given symbol in the view for the request-map." }
  render-by-symbol [symbol-name & params]
  (when (or (environment/reload-files?) (not (view-loaded?)))
    (load-view))
  (apply
    (ns-resolve (get-view-ns) symbol-name) params))

(defn
#^{ :doc "Returns the rendered view for the request-map." }
  render-view [& params]
  (apply render-by-symbol 'render-view params))

(defn
#^{ :doc "Returns the string rendered view for the request-map." }
  render-str [& params]
  (apply render-by-symbol 'render-str params))

(defn
#^{ :doc "Returns the body rendered view for the request-map." }
  render-body [& params]
  (apply render-by-symbol 'render-body params))

(defn-
#^{ :doc "Returns a new request-map for use when rendering a layout. The new map is similar to the request-map 
except the controller is \"layouts\", the action is layout-name, and layout-info contains the controller and action from
the original request-map." }
  merge-layout-request-map [layout-name]
  (merge request/request-map
    { :controller "layouts", 
      :action layout-name 
      :layout-info 
        (merge
          (:layout-info request/request-map)
          (select-keys request/request-map [:controller :action :params])) }))

(defn-
#^{:doc "Returns the rendered layout for the given layout name with the given body."}
  render-layout-body [layout-name body]
  (request/set-request-map (merge-layout-request-map layout-name)
    (render-body
      (if (map? body)
        (:body body)
        body))))

(defn
#^{:doc "Returns the rendered layout for the given layout name and the given body."}
  render-layout [layout-name body]
  (if layout-name
    (let [full-body (render-layout-body layout-name body)]
      (if (map? body)
        (assoc body :body full-body)
        full-body))
    body))

(defn
#^{:doc "Returns the full host string from the given params. Used by url-for." }
  full-host []
  (let [server-name (request/server-name)
        user (request/user)
        password (request/password)
        url-port (request/url-port)]
    (if (and server-name (not (request/only-path?)))
      (str 
        (request/scheme) "://" 
        (if (and user password) (str user ":" password "@")) 
         server-name
        (when url-port
          (str ":" url-port))))))

(defn-
#^{ :doc "Returns the anchor from the request map and adds a '#' before it. If the key :anchor does not 
exist, then this method returns nil. This method is used by url-for." }
  anchor []
  (let [anchor (request/anchor)]
    (when anchor
      (str "#" anchor))))

(defn
  servlet-path []
  (when-let [servlet-context (request/servlet-context)]
    (when (servlet-utils/servlet-uri? servlet-context (request/uri))
      (servlet-utils/servlet-uri-path servlet-context))))

(defn
#^{ :doc "Propagates the session id parameter (if the session cookie is not used) to the given request-map and returns
the result." }
  propagate-session-id [original-request-map request-map]
  (if session-config/use-session-cookie
    request-map
    (assoc request-map :params 
      (merge { :session-id (session-utils/session-id original-request-map) } (:params request-map)))))

(defn
#^{ :doc "Returns the params merged with the given request-map. Only including the keys from request-map used by url-for" }
  merge-url-for-params [request-map params]
  (propagate-session-id
    request-map
    (merge 
      (select-keys request-map [:controller :action :request :temp-session])
      params)))

(defn
#^{ :doc "Returns the request controller as a string." }
  controller-str []
  (conjure-str-utils/str-keyword (request/controller)))

(defn
#^{ :doc "Returns the request action as a string." }
  action-str []
  (conjure-str-utils/str-keyword (request/action)))

(defn
#^{ :doc "Returns a path created from the the request." }
  create-path []
  (interleave 
    (repeat "/") 
    (filter identity
      [ (servlet-path)
        (loading-utils/dashes-to-underscores (controller-str))
        (loading-utils/dashes-to-underscores (action-str))
        (request/id-str)
        (anchor) ])))

(defn
#^{ :doc "Returns the session id url parameter for use in url-for, if and only if session-config/use-session-cookie is
false." }
  session-url-param [url-params]
  (when-not session-config/use-session-cookie
    (let [session-id (session-utils/session-id)
          new-session-id (or session-id (session-utils/temp-session-id) (session-utils/create-session-id))
          new-url-params 
            (if new-session-id (assoc url-params :session-id new-session-id) url-params)]
      (when (not-empty new-url-params)
        (html-utils/url-param-str new-url-params)))))

(defn
  create-url-params []
  (let [clean-url-params (or (dissoc (request/parameters) :id) {})]
    (or
      (session-url-param clean-url-params)
      (html-utils/url-param-str clean-url-params))))

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
  ([] (url-for {})) 
  ([params]
    (request/with-request-map-fn #(merge-url-for-params % params)
      (if (and (request/controller) (request/action))
        (apply str 
          (flatten
            [ (full-host)
              (create-path)
              (create-url-params) ]))
        (throw (new RuntimeException (str "You must pass a controller and action to url-for. " request/request-map)))))))