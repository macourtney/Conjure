(ns conjure.server.jetty-server
  (:import [java.io File]
           [javax.servlet.http HttpServlet HttpServletRequest HttpServletResponse]
           [org.mortbay.jetty Server Handler Connector NCSARequestLog]
           [org.mortbay.jetty.handler HandlerCollection ContextHandlerCollection RequestLogHandler]
           [org.mortbay.jetty.nio BlockingChannelConnector]
           [org.mortbay.jetty.servlet Context DefaultServlet ServletHolder SessionHandler])
  (:use [conjure.util string-utils]
        [conjure.server server servlet]))

;; create connector on the specified port
(defn make-connectors [port]
  (let [conn (new BlockingChannelConnector)]
    (. conn (setPort port))
    (into-array [conn])))

;; configures a default servlet to serve static pages from the "/" directory
(defn configure-context-handlers [contexts]
  (let [context (new Context contexts "/" (. Context NO_SESSIONS))]
    (. context (setWelcomeFiles (into-array ["index.html"])))
    (. context (setResourceBase (. (new File (:static-dir (http-config))) (getPath))))
    (. context (setSessionHandler (new SessionHandler)))
    (. context (addServlet (new ServletHolder (new DefaultServlet)) "/*"))
    (. context (addServlet (new ServletHolder (make-servlet)) "/*"))
    context))

;; set up hanlders: the context ones, and a logger to track all http requests
(defn make-handlers [contexts]
  (let [handlers (new HandlerCollection)
        logger (new RequestLogHandler)
        http-config-map (http-config)
        logfile (new File (:log-dir http-config-map) (:log-pattern http-config-map))]

    (. logger (setRequestLog (new NCSARequestLog (. logfile (getPath)))))
    (. handlers (addHandler logger))
    (. handlers (addHandler contexts))
    handlers))

;; make an empty collection of context handers - we'll confiture it later
(defn make-contexts []
  (new ContextHandlerCollection))

;; make an instance of the http server
(defn make-server
  ([] (make-server (:port (http-config))))
  ([port]
    (do
      (config-server)
      (let [contexts (make-contexts)
            connectors (make-connectors port)
            handlers (make-handlers contexts)
            server (new Server)]
        (. server (setConnectors connectors))
        (. server (setHandler handlers))
        (configure-context-handlers contexts)
        server))))

