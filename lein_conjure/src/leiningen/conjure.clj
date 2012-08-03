(ns leiningen.conjure
  (require [conjure.execute :as conjure-execute]
           [leiningen.core.eval :as lein-eval]))

(defn
  ^ {:doc "A Leiningen plugin to do common conjure tasks."}
  conjure [project & args]
  (lein-eval/eval-in-project project
    `(apply conjure.execute/-main '~args)
    '(require 'conjure.execute)))
