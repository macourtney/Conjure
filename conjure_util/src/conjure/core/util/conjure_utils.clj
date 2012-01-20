(ns conjure.core.util.conjure-utils
  (:import [java.io File])
  (:require [clojure.set :as clj-set]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.logging :as logging]
            [clojure.tools.namespace :as clojure-namespace]))

(def loaded-namespaces (atom {}))

(defn
#^{ :doc "Returns true if the given var-name is in a conjure namespace (controller, helper, model or view)." }
  conjure-namespace? [var-name]
  (or 
    (.startsWith var-name "controllers.")
    (.startsWith var-name "helpers.")
    (.startsWith var-name "models.")
    (.startsWith var-name "views.")))

(defn
#^{ :doc "Returns a set of conjure namespaces (controllers, models, helpers and views) used by the given namespace." }
  conjure-namespaces [namespace-name]
  (let [namespace-to-search (find-ns (symbol namespace-name))]
    (reduce
      (fn [namespace-set var-name] 
        (conj namespace-set 
          (let [slash-index (.indexOf var-name "/")]
            (if (> slash-index 0) 
              (.substring var-name 0 slash-index)
              var-name))))
      #{}
      (filter 
        conjure-namespace?
        (concat 
          (map str (vals (ns-aliases namespace-to-search)))
          (map #(.substring (str %) 2) 
            (filter #(not (.startsWith (str %) "#'clojure"))
              (vals (ns-refers namespace-to-search)))))))))

(defn create-file-namespace-map
  "Returns a file namespace map from the given .clj file. The file namespace map includes the file, and the namespace from the file."
  [^File clj-file]
  { :file clj-file
    :namespace (second (clojure-namespace/read-file-ns-decl clj-file)) })

(defn file-namespaces
  "Returns a list of the file namespace map for all files in the classpath."
  []
  (map create-file-namespace-map
    (mapcat clojure-namespace/find-clojure-sources-in-dir (loading-utils/classpath-directories))))

(defn filter-file-namespaces
  "Returns a list of the file namespace maps for all of the given namespaces which can be found as files in the classpath."
  [namespace-names]
  (let [namespace-names-set (set namespace-names)]
    (if (not-empty namespace-names-set)
      (filter #(contains? namespace-names-set (str (:namespace %))) (file-namespaces))
      '())))

(defn namespace-map [namespace-name]
  (first (filter-file-namespaces [namespace-name])))

(defn clear-loaded-namespaces
  "Clears all namespaces from the set of loaded namespaces."
  []
  (reset! loaded-namespaces {}))

(defn namespace-loaded?
  "Returns true if the given namespace name has already been loaded."
  [namespace-name]
  (contains? @loaded-namespaces namespace-name))

(defn namespace-load-info
  "Returns the namespace load info map for the given namespace name from the set of loaded namespaces."
  [namespace-name]
  (get @loaded-namespaces namespace-name))

(defn last-modified
  "Returns the last modified date (as a long) of the file in the given namespace map."
  [namespace-map]
  (.lastModified (:file namespace-map)))

(defn namespace-name [namespace-map]
  (str (:namespace namespace-map)))

(defn namespace-info
  "Converts the given namespace map in to a namespace info map for use in the list of loaded namespaces."
  [namespace-map]
  (if (not (contains? namespace-map :last-modified))
    (assoc namespace-map :last-modified (last-modified namespace-map))
    namespace-map))

(defn add-namespace-info
  "Adds the given namespace map to the list of loaded namespaces."
  [namespace-map]
  (let [new-namespace-info (namespace-info namespace-map)]
    (swap! loaded-namespaces #(assoc % (namespace-name new-namespace-info) new-namespace-info))))

(defn add-namespace-infos
  "Adds the given namespace maps to the list of loaded namespaces."
  [namespace-maps]
  (doseq [namespace-map namespace-maps]
    (add-namespace-info namespace-map)))

(defn reload-namespace-map?
  "Returns true if the file referenced by the given namespace map has been changed since the last time it was loaded.
If the given namespace is not in the list of loaded namespaces, it is added. An attempt is made to find the namespace.
If the attempt fails, this function returns true."
  [namespace-map]
  (if-let [namespace-info (namespace-load-info (namespace-name namespace-map))]
    (< (:last-modified namespace-info) (last-modified namespace-map))
    (if (find-ns (:namespace namespace-map))
      (do
        (add-namespace-info namespace-map)
        false)
      true)))

(defn reload-namespace?
  "Returns true if the given namespace should be reloaded. All of the rules in reload-namespace-map? apply to this
function as well."
  [namespace-name]
  (when-let [new-namespace-map (namespace-map namespace-name)]
    (reload-namespace-map? new-namespace-map)))

(defn namespaces-to-reload
  "Returns a list of all the given namespaces which should be reloaded."
  [namespace-names]
  (filter reload-namespace-map? (filter-file-namespaces namespace-names)))

(defn reload-namespaces
  "Reloads the given namespace and makes sure the namespace is added to the list of loaded namespaces.
Namespaces-to-reload must be a collection of namespace maps."
  [namespaces-to-reload]
  (loading-utils/reload-namespaces (map namespace-name namespaces-to-reload))
  (add-namespace-infos namespaces-to-reload))

(defn reload-conjure-namespace
  "Reloads the given namespace if and only if the namespace is a conjure namespace and it needs to be reloaded
according to reload-namespace?"
  [namespace-name]
  (when (conjure-namespace? namespace-name)
    (when-let [new-namespace-map (namespace-map namespace-name)]
      (when (reload-namespace-map? new-namespace-map)
        (reload-namespaces [new-namespace-map])))))

(defn
#^{ :doc "Reloads all of the conjure namespaces refered to by the namespace with the given name." }
  reload-conjure-namespaces
  [namespace-name-to-reload]
  (reload-conjure-namespace namespace-name-to-reload) 
  (let [namespaces-to-load (namespaces-to-reload (conjure-namespaces namespace-name-to-reload))]
    (when (not-empty namespaces-to-load)
      (reload-namespaces namespaces-to-load)
      (doseq [child-namespace namespaces-to-load]
        (reload-conjure-namespaces (namespace-name child-namespace))))))