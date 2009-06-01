(ns conjure.util.string-utils)

(defn tokenize [string delimiter]
  (let [tokenizer (new java.util.StringTokenizer string delimiter)]
    (if (. tokenizer hasMoreTokens)
      (loop [out (cons (. tokenizer nextToken) ())]
        (if (. tokenizer hasMoreTokens)
          (recur (cons (. tokenizer nextToken) out))
          (reverse out)))
      nil)))