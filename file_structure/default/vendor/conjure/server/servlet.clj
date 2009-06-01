(ns conjure.server.servlet
  (:import [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse])
  (:use [conjure.util string-utils]
        [conjure.server server]))

;; the method which produces the page for the test servlet.
(defn process [#^HttpServletRequest req #^HttpServletResponse resp]
  (let [out (. resp (getOutputStream))
        path_tokens (tokenize (. req getPathInfo) "/")
        controller (nth path_tokens 0 nil)
        action (nth path_tokens 1 nil)]
    (load-controller (controller-file-name controller))
    (. out (println (load-string (fully-qualified-action controller action ))))))

;; implementation of an HttpServlet, overriding just on function:
;;   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
(defn make-servlet []
  (proxy [HttpServlet] []
    (doGet [#^HttpServletRequest req #^HttpServletResponse resp]
      (process req resp))))
