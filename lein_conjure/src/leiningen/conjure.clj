(ns leiningen.conjure
  (require [conjure.core.execute :as conjure-execute]
           [leiningen.compile :as lein-compile]))

(defn
  ^ {:doc "A Leiningen plugin to do common conjure tasks."}
  conjure [project & args]
  (lein-compile/eval-in-project project
    `(do
      (use ~''conjure.core.execute)
      (apply conjure.core.execute/-main '~args))))
