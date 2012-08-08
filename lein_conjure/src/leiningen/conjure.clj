(ns leiningen.conjure
  (require [leiningen.core.eval :as lein-eval]))

(defn
  ^ {:doc "A Leiningen plugin to do common conjure tasks."}
  conjure [project & args]
  (lein-eval/eval-in-project project
    `(conjure.util.execute-utils/run-args '~args)
    '(require 'conjure.util.execute-utils)))
