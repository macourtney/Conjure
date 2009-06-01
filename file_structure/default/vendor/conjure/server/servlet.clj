(ns conjure.server.servlet
  (:import [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse])
  (:use [conjure.server.server :as server]))

;; the method which produces the page for the test servlet.
(defn process [#^HttpServletRequest req #^HttpServletResponse resp]
  (let [out (. resp (getOutputStream))
        route-map (server/create-route-map (. req getPathInfo))]

    (load-controller (server/controller-file-name route-map))
    (. out (println (load-string (server/fully-qualified-action route-map))))))

;; implementation of an HttpServlet, overriding just on function:
;;   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
(defn make-servlet []
  (proxy [HttpServlet] []
    (doGet [#^HttpServletRequest req #^HttpServletResponse resp]
      (process req resp))))
