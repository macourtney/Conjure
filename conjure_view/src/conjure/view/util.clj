(ns conjure.view.util
  (:require [clojure.tools.logging :as logging]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.html-utils :as html-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.servlet-utils :as servlet-utils]
            [clojure.tools.string-utils :as conjure-str-utils]
            [config.session-config :as session-config]
            [conjure.config.environment :as environment]
            [conjure.util.request :as request]
            [conjure.util.conjure-utils :as conjure-utils]
            [conjure.util.session-utils :as session-utils]))

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
#^{ :doc "Finds a service directory for the given service in the given views directory." }
  find-service-directory 
  ([service] (find-service-directory (find-views-directory) service))
  ([view-directory service]
    (when service
      (file-utils/find-directory view-directory (loading-utils/dashes-to-underscores service)))))
  
(defn
#^{ :doc "Finds a view file with the given view-directory and action." }
  find-view-file [service-directory action]
  (when (and service-directory action)
    (file-utils/find-file service-directory (loading-utils/symbol-string-to-clj-file action))))

(defn
#^{:doc "Returns the view namespace request map."}
  request-view-namespace 
  ([] (request-view-namespace (request/service) (request/action)))
  ([service action]
  (when (and service action)
    (str "views." (loading-utils/underscores-to-dashes service) "." 
      (loading-utils/underscores-to-dashes (conjure-str-utils/str-keyword action))))))

(defn
#^{:doc "Returns the view namespace for the given service and action."}
  view-namespace-by-action [service action]
  (when (and service action)
    (request-view-namespace service action)))

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
#^{ :doc "Adds the given service and action to the given loaded views map." }
  assoc-loaded-views [loaded-view-map service action]
  (let [service-key (keyword service)]
    (assoc loaded-view-map service-key 
      (add-loaded-action (get loaded-view-map service-key) action))))

(defn
#^{ :doc "Loads the view corresponding to the values in the given request map." }
  load-view 
  ([] (load-view (request/service) (request/action)))
  ([service action] (load-view service action (environment/reload-files?)))
  ([service action reload?]
    (let [view-namespace (request-view-namespace service action)]
      (if reload?
        (conjure-utils/reload-conjure-namespaces view-namespace)
        (require (symbol view-namespace)))
      (reset! loaded-views (assoc-loaded-views @loaded-views service action)))))

(defn clear-loaded-views
  "Clears the list of loaded views. After calling this function view-loaded? should return false for all views."
  []
  (reset! loaded-views {}))

(defn
#^{ :doc "Returns true if the view corresponding to the request-map or given service and action is already loaded." }
  view-loaded?
  ([] (view-loaded? (request/service) (request/action)))
  ([service action]
    (contains? (get @loaded-views (keyword service)) (keyword action))))

(defn
  #^{ :doc "Returns the namespace of the view for the request map. The namespace is an actual namespace object." }
  get-view-ns []
  (find-ns (symbol (request-view-namespace))))

(defn
  #^{ :doc "Calls the render function with the given symbol in the view for the request-map." }
  render-by-symbol [symbol-name params]
  (when (or (environment/reload-files?) (not (view-loaded?)))
    (load-view))
  (apply (ns-resolve (get-view-ns) symbol-name) params))

(defn
#^{ :doc "Returns the rendered view for the request-map." }
  render-view [& params]
  (render-by-symbol 'render-view params))

(defn
#^{ :doc "Returns the string rendered view for the request-map." }
  render-str [& params]
  (render-by-symbol 'render-str params))

(defn
#^{ :doc "Returns the body rendered view for the request-map." }
  render-body [& params]
  (render-by-symbol 'render-body params))

(defn-
#^{ :doc "Returns a new request-map for use when rendering a layout. The new map is similar to the request-map 
except the service is \"layouts\", the action is layout-name, and layout-info contains the service and action from
the original request-map." }
  merge-layout-request-map [layout-name]
  (merge request/request-map
    { :service "layouts"
      :action layout-name 
      :layout-info 
        (merge
          (:layout-info request/request-map)
          (select-keys request/request-map [:service :service :action :params])) }))

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

