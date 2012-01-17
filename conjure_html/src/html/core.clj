(ns html.core
  (:import [clojure.lang IPersistentVector ISeq]))

;; Pulled from old-contrib to avoid dependency
(defn as-str
  ([] "")
  ([x] 
    (if (instance? clojure.lang.Named x)
      (name x)
      (str x)))
  ([x & ys]
    ((fn [^StringBuilder sb more]
      (if more
        (recur (. sb (append (as-str (first more)))) (next more))
        (str sb)))
      (new StringBuilder ^String (as-str x)) ys)))

(defn escape-xml
  "Change special characters into HTML character entities."
  [text]
  (.. ^String (as-str text)
    (replace "&" "&amp;")
    (replace "<" "&lt;")
    (replace ">" "&gt;")
    (replace "\"" "&quot;")))

(defn render-obj [value]
  (if (keyword? value)
    (as-str value)
    (escape-xml value)))

(defn xml-attribute [name value]
  (list " " (render-obj name) "=\"" (render-obj value) "\""))

(defn render-attribute [[name value]]
  (cond
    (true? value) (xml-attribute name name)
    (not value) [""]
    :else (xml-attribute name value)))

(defn render-attr-map [attrs]
  (mapcat render-attribute attrs))

(def ^{:doc "Regular expression that parses a CSS-style id and class from a tag name." :private true}
  re-tag #"([^\s\.#]+)(?:#([^\s\.#]+))?(?:\.([^\s#]+))?")

(def ^{:doc "A list of tags that need an explicit ending tag when rendered." :private true}
  container-tags
  #{"a" "b" "body" "canvas" "dd" "div" "dl" "dt" "em" "fieldset" "form" "h1" "h2" "h3" "h4" "h5" "h6" "head" "html" "i"
    "iframe" "label" "li" "ol" "option" "pre" "script" "span" "strong" "style" "table" "textarea" "ul"})

(defn create-element [tag attrs content]
  (if content
    [tag attrs content]
    [tag attrs]))

(defn normalize-element
  "Ensure a tag vector is of the form [tag-name attrs content]."
  [[tag & content]]
  (when (not (or (keyword? tag) (symbol? tag) (string? tag)))
    (throw (IllegalArgumentException. (str tag " is not a valid tag name."))))
  (let [[_ tag id class] (re-matches re-tag (render-obj tag))
        id-tag-attrs (if id { :id id } {})
        tag-attrs (if class (assoc id-tag-attrs :class (.replace ^String class "." " ")) id-tag-attrs)
        map-attrs (first content)]
    (if (map? map-attrs)
      (create-element tag (merge tag-attrs map-attrs) (next content))
      (create-element tag tag-attrs content))))

(defmulti render-xml
  "Turn a Clojure data type into a string of HTML."
  type)

(defn- render-element
  "Render an tag vector as a HTML element."
  [element]
  (let [[tag attrs content] (normalize-element element)]
    (flatten
      (if (or content (container-tags tag))
        (list "<" tag (render-attr-map attrs) ">"
             (render-xml content)
             "</" tag ">")
        (list "<" tag (render-attr-map attrs) " />")))))

(defmethod render-xml IPersistentVector
  [element]
  (render-element element))

(defmethod render-xml ISeq [coll]
  (mapcat render-xml coll))

(defmethod render-xml :default [x]
  (list (render-obj x)))