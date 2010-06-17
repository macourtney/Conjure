(ns conjure.core.plugin.base
  (:require [conjure.core.plugin.util :as plugin-util]))

(defmacro
#^{ :doc "Returns plugin name for this plugin." }
  plugin-name []
  (plugin-util/plugin-name-from-namespace (name (ns-name *ns*))))