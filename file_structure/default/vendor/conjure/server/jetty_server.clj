(ns conjure.server.jetty-server
  (:import [java.io File]
           [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse]
           [org.mortbay.jetty Server Handler Connector NCSARequestLog Request]
           [org.mortbay.jetty.handler HandlerCollection ContextHandlerCollection RequestLogHandler AbstractHandler]
           [org.mortbay.jetty.nio BlockingChannelConnector]
           [org.mortbay.jetty.servlet Context DefaultServlet ServletHolder SessionHandler])
  (:require [conjure.util string-utils]
            [conjure.server.server :as server]
            [conjure.server.servlet :as servlet]
            [clojure.contrib.str-utils :as str-utils]))

(defn
#^{:doc "Create a connector on the specified port."}
  make-connectors [port]
  (let [conn (new BlockingChannelConnector)]
    (. conn (setPort port))
    (into-array [conn])))

(defn
#^{:doc "Configures a default servlet to serve static pages from the "/" directory. And adds the conjure servlet."}
  configure-context-handlers [contexts]
  (let [context (new Context contexts "/" (. Context NO_SESSIONS))]
    (. context (setWelcomeFiles (into-array ["index.html"])))
    (. context (setResourceBase (. (new File (:static-dir (server/http-config))) (getPath))))
    (. context (setSessionHandler (new SessionHandler)))
    (. context (addServlet (new ServletHolder (new DefaultServlet)) "/*"))
    context))
    
(defn
#^{:doc "Returns a map of parameters pulled from the query string of the given request."}
  load-query-params [request]
  (let [request-query (. request getQueryString)]
    (server/parse-query-params request-query)))
    
(defn
#^{:doc "Returns a map of parameters pulled from the given request."}
  load-params [request]
  (let [jetty-params (. request getParameters)]
    (if jetty-params
      (let [jetty-param-names (. jetty-params keySet)]
        (loop [current-param (first jetty-param-names)
               params-rest (rest jetty-param-names)
               curent-values (. jetty-params getValues current-param)
               output { current-param (seq curent-values) }]
          (println "current-param:" current-param)
          (if (not-empty params-rest)
            (recur (first params-rest) 
                   (rest params-rest) 
                   (. jetty-params getValues current-param)
                   (assoc output current-param (seq curent-values)))
            (merge output (load-query-params request)))))
      (load-query-params request))))

(defn
#^{:doc "Creates a handler for conjure requests."}
  create-conjure-handler []
  (proxy [AbstractHandler] []
    (handle [target #^HttpServletRequest request #^HttpServletResponse response dispatch]
      (let [output (server/process-request (. request getPathInfo) (load-params request))]
        (if output
          (do
            (. response setContentType "text/html")
            (. response setStatus (. HttpServletResponse SC_OK))
            (. (. response getWriter) println output)
            (. request setHandled true)))))))

(defn
#^{:doc "Set up hanlders: the context ones, and a logger to track all http requests."}
  make-handlers [contexts]
  (let [handlers (new HandlerCollection)
        logger (new RequestLogHandler)
        http-config-map (server/http-config)
        logfile (new File (:log-dir http-config-map) (:log-pattern http-config-map))]

    (. logger (setRequestLog (new NCSARequestLog (. logfile (getPath)))))
    (. handlers (addHandler logger))
    (. handlers (addHandler (create-conjure-handler)))
    (. handlers (addHandler contexts))
    handlers))

(defn
#^{:doc "Makes an empty collection of context handers."}
  make-contexts []
  (new ContextHandlerCollection))

(defn
#^{:doc "Makes an instance of the http server."}
  make-server
  ([] (make-server (:port (server/http-config))))
  ([port]
    (do
      (server/config-server)
      (let [contexts (make-contexts)
            connectors (make-connectors port)
            handlers (make-handlers contexts)
            server (new Server)]
        (. server (setConnectors connectors))
        (. server (setHandler handlers))
        (configure-context-handlers contexts)
        server))))

