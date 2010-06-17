(ns leiningen.conjure
  (require [conjure.core.execute :as execute]))

(defn conjure [projects & args]
   (apply execute/-main args))
