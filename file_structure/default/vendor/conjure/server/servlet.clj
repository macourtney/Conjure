(ns conjure.server.servlet
  (:import [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse])
  (:use [conjure.server.server :as server]))

(defn
#^{:doc "Processes the request by parsing the associated path..."}
  process [#^HttpServletRequest req #^HttpServletResponse resp]
  (let [out (. resp (getOutputStream))]
    (. out (println (server/process-request (. req getPathInfo) {})))))

;; implementation of an HttpServlet, overriding just on function:
;;   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
(defn
#^{:doc "implementation of an HttpServlet, overriding just on function:
           protected void doGet(HttpServletRequest req, HttpServletResponse resp)."}
  make-servlet []
  (proxy [HttpServlet] []
    (doGet [#^HttpServletRequest req #^HttpServletResponse resp]
      (process req resp))))