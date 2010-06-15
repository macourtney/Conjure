(ns leiningen.conjure
  (require [conjure.execute :as execute]))

(defn conjure [projects & args]
   (apply execute/-main args))
