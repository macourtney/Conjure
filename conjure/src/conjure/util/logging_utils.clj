(ns conjure.util.logging-utils
  (import [java.util Properties]
          [java.io ByteArrayOutputStream ByteArrayInputStream]
          [java.util.logging LogManager]))

(defn
#^{ :doc "Initializes the logger using the given configuration map." }
  load-configuration-map [config-map]
  (let [properties (new Properties)
        output-stream (new ByteArrayOutputStream)]
    (doall (map (fn [pair] (.setProperty properties (first pair) (second pair))) config-map))
    (.store properties output-stream "")
    (.. LogManager (getLogManager) (readConfiguration (new ByteArrayInputStream (. output-stream toByteArray))))))