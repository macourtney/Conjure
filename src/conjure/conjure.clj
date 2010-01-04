(ns conjure
  (:gen-class)
  (:use clojure.contrib.command-line))

(defn -main [& args]
  (with-command-line args
      "Command line demo"
      [[foo "This is the description for foo" 1]
       [bar "This is the description for bar" 2]
       [boolean? b? "This is a boolean flag."]
       remaining]
    (println "foo: " foo)
    (println "bar: " bar)
    (println "boolean?: " boolean?)
    (println "remaining: " remaining)))
