(ns conjure.util.java-utils)

(defn enumeration-as-list [enumeration]
  (if (. enumeration hasMoreElements) 
    (loop [output (cons (. enumeration nextElement) ())]
      (if (. enumeration hasMoreElements)
        (recur (cons (. enumeration nextElement) output))
        output))
    ()))