(ns conjure.core.server.servlet
  (:import [java.io FileNotFoundException])
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:require [ring.util.servlet :as ring-servlet]))

(defn
  conjure-servlet [req]
  (let [adaptor-namespace-symbol 'conjure.core.server.ring-adapter]
    (try
      (require adaptor-namespace-symbol)
      (catch FileNotFoundException e))
    (if-let [adaptor-namespace (find-ns adaptor-namespace-symbol)]
      (if-let [adaptor-fn (ns-resolve adaptor-namespace 'conjure)]
        (adaptor-fn req)
        (println "Could not find the conjure ring adaptor function."))
      (println "Could not find the conjure ring adaptor namespace.")))) 

(ring-servlet/defservice conjure-servlet)