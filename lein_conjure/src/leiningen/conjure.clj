(ns leiningen.conjure
  (require [leiningen.compile :as lein-compile]))

(defn conjure [project & args]
  (lein-compile/eval-in-project project
    `(do
      (use ~''conjure.core.execute)
      (apply conjure.core.execute/-main '~args))))
