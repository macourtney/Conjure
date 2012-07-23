(ns conjure.script.destroyers.view-destroyer
  (:require [clojure.tools.logging :as logging]
            [conjure.view.util :as util]
            [conjure.script.destroyers.view-test-destroyer :as view-test-destroyer]
            [conjure.script.view.util :as script-view-util]))

(defn
#^{:doc "Destroys the view file from the given service and action."}
  destroy-view-file 
  ([service action] (destroy-view-file service action false))
  ([service action silent]
    (if (and service action)
      (when-let [view-file (script-view-util/find-view-file service action)]
        (let [is-deleted (.delete view-file)] 
          (logging/info (str "File " (.getName view-file) (if is-deleted " destroyed." " not destroyed.")))))
      (script-view-util/destroy-usage "view"))))

(defn
#^{:doc "Destroys a view file for the view name given in params."}
  destroy [params]
  (destroy-view-file (first params) (second params)))

(defn
#^{:doc "Destroys all of the files created by the view_generator."}
  destroy-all-dependencies
  ([service action] (destroy-all-dependencies service action false))
  ([service action silent]
    (destroy-view-file service action silent)
    (view-test-destroyer/destroy-all-dependencies service action silent)))