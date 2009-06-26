(ns conjure.view.base)

(defmacro
#^{:doc "Defines a view. This macro should be used in a view file to define the parameters used in the view."}
  defview [params & body]
  (let [render-view "render-view"
        request-map "request-map"]
    `(defn ~'render-view [~'request-map ~@params]
      ~@body)))