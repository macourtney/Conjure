(ns conjure.core.util.conjure-utils
  (:require [clojure.contrib.ns-utils :as ns-utils]
            [clojure.set :as clj-set]
            [clojure.tools.loading-utils :as loading-utils]))

(defn
#^{ :doc "Returns true if the given var-name is in a conjure namespace (controller, helper, model or view)." }
  conjure-namespace? [var-name]
  (or 
    (.startsWith var-name "controllers.")
    (.startsWith var-name "helpers.")
    (.startsWith var-name "models.")
    (.startsWith var-name "views.")))

(defn
#^{ :doc "Returns a set of conjure namespaces (controllers, models, helpers and views) used by the given controller." }
  conjure-namespaces [namespace-name]
  (let [namespace-to-search (ns-utils/get-ns (symbol namespace-name))]
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

(defn
#^{ :doc "Reloads all of the conjure namespaces refered to by the namespace with the given name." }
  reload-conjure-namespaces
  ([namespace-name] (reload-conjure-namespaces namespace-name #{}))
  ([namespace-name loaded-namespaces]
    (let [namespaces-to-load (filter #(not (contains? loaded-namespaces %)) (conjure-namespaces namespace-name))]
      (when (not-empty namespaces-to-load)
        (loading-utils/reload-namespaces namespaces-to-load)
        (doseq [child-namespace namespaces-to-load]
          (reload-conjure-namespaces child-namespace (clj-set/union loaded-namespaces (set namespaces-to-load))))))))