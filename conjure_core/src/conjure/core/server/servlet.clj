(ns conjure.core.server.servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:require [conjure.core.server.ring-adapter :as ring-adapter]
            [ring.util.servlet :as ring-servlet]))

(ring-servlet/defservice ring-adapter/conjure)