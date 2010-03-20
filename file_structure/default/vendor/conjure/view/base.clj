(ns conjure.view.base
  (:use clj-html.core)
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.util.html-utils :as html-utils]
            environment))

(defmacro
#^{ :doc "Defines a view. This macro should be used in a view file to define the parameters used in the view." }
  defview [params & body]
  `(defn ~'render-view [~'request-map ~@params]
    ~@body))

(defn- 
#^{ :doc "If function is a function, then this method evaluates it with the given args. Otherwise, it just returns
function." }
  evaluate-if-fn [function & args]
  (if (fn? function)
    (apply function args)
    function))

(defn-
#^{ :doc "Replaces the current extension on source with the given extension." }
  replace-extension [source extension]
  (if extension
    (conjure-str-utils/add-ending-if-absent
      (str-utils/re-sub #"\.[a-zA-Z0-9]*$" "" source) 
      (if extension (str "." extension)))
    source))

(defn
#^{ :doc "Returns a path for the given source in the given base-dir with the given extension (if none is given)." }
  compute-public-path 
  ([source base-dir] (compute-public-path source base-dir nil))
  ([source base-dir extension]
    (replace-extension
      (if (. source startsWith "/")
        source
        (if (. source startsWith "http://") ; This should probably check for ftp, https, and etc.
          source
          (str "/" base-dir "/" source)))
      extension)))

(defn
#^{ :doc "Returns the full path to the given image source." }
  image-path [source]
    (compute-public-path source environment/images-dir))
  
(defn
#^{ :doc "Returns an image tag for the given source and with the given options." }
  image-tag 
  ([source] (image-tag source {}))
  ([source html-options] [:img (merge { :src (image-path source) } html-options)]))

(defn
#^{ :doc "Returns the full path to the given stylesheet source." }
  stylesheet-path [source]
    (compute-public-path source environment/stylesheets-dir "css"))

(defn-
#^{ :doc "Returns the type of the first parameter." }
  first-type [& params]
  (class (first params)))

(defmulti 
#^{ :doc "Returns a stylesheet tag for the given source and with the given options." }
  stylesheet-link-tag first-type)
  
(defmethod stylesheet-link-tag clojure.lang.PersistentVector
  ([sources] (stylesheet-link-tag sources {}))
  ([sources html-options]
    (map stylesheet-link-tag sources (repeat html-options))))
  
(defmethod stylesheet-link-tag String
  ([source] (stylesheet-link-tag source {}))
  ([source html-options]
    [:link 
      (merge 
        { :href (stylesheet-path source), 
          :media "screen", 
          :rel "stylesheet", 
          :type "text/css" } 
        html-options)]))

(defn
#^{ :doc "Returns the full path to the given javascript source." }
  javascript-path [source]
    (compute-public-path source environment/javascripts-dir "js"))

(defmulti
#^{ :doc "Returns a javascript include tag for the given source and with the given options." }
  javascript-include-tag first-type)

(defmethod javascript-include-tag clojure.lang.PersistentVector
  ([sources] (javascript-include-tag sources {}))
  ([sources html-options]
    (map javascript-include-tag sources (repeat html-options))))

(defmethod javascript-include-tag String
  ([source] (javascript-include-tag source {}))
  ([source html-options]
    [:script
      (merge 
        { :src (javascript-path source),
          :type "text/javascript" } 
        html-options) ""]))

(defn
#^{ :doc "Returns a jquery javascript include tag with the optional given options." } 
  jquery-include-tag
  ([] (jquery-include-tag {}))
  ([html-options]
    (javascript-include-tag environment/jquery html-options)))
    
(defn
#^{ :doc "Returns a jquery javascript include tag with the optional given options." } 
  conjure-js-include-tag
  ([] (conjure-js-include-tag {}))
  ([html-options]
    (javascript-include-tag environment/conjure-js html-options)))

(defn
#^{ :doc "Returns a mailto link with the given mail options. Valid mail options are:

  :address - The full e-mail address to use. (required)
  :name - The display name to use. If not given, address is used.
  :html-options - Any extra attributes for the mail to tag.
  :replace-at - If name is not given, then replace the @ symbol with this text in the address before using it as the name.
  :replace-dot - If name is not given, then replace the . in the email with this text in the address before using it as the name.
  :subject - Presets the subject line of the e-mail.
  :body - Presets the body of the email.
  :cc - Carbon Copy. Adds additional recipients to the email.
  :bcc - Blind Carbon Copy. Adds additional hidden recipients to the email." }
  mail-to [mail-options] 
   (let [address (:address mail-options)
         display-name 
          (or 
            (:name mail-options) 
            (conjure-str-utils/str-replace-if address { "@" (:replace-at mail-options), "." (:replace-dot mail-options) }))
         mailto-params (html-utils/url-param-str (select-keys mail-options [:cc :bcc :subject :body]))]
     [:a
       (merge
         { :href (str "mailto:" address mailto-params) }
         (:html-options mail-options))
       display-name]))
         
(defn
#^{ :doc "Returns an xml header tag with the given html-options. If no html-options are given, then the tag is created 
with the following defaults:

  version=\"1.0\"" }
  xml-header-tag 
  ([] (xml-header-tag {}))
  ([html-options]
    (str 
      "<?xml " 
      (html-utils/attribute-list-str (merge { :version "1.0" } html-options ))
      "?>")))

(defn
#^{ :doc "Returns the html doc type tag. You can pass a type into this method for a specific type. Valid types are:

  :html4.01-strict
  :html4.01-transitional
  :html4.01-frameset
  :xhtml1.0-strict
  :xhtml1.0-transitional - default
  :xhtml1.0-frameset
  :xhtml1.1" }
  html-doctype
  ([] (html-doctype :xhtml1.0-transitional)) 
  ([doc-type]
    (cond
      (= doc-type :html4.01-strict) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"
      (= doc-type :html4.01-transitional) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
      (= doc-type :html4.01-frameset) 
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">"
      (= doc-type :xhtml1.0-strict) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
      (= doc-type :xhtml1.0-transitional) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
      (= doc-type :xhtml1.0-frameset) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"
      (= doc-type :xhtml1.1) 
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
      true (throw (RuntimeException. (str "Unknown doc type: " doc-type))))))

(require 'conjure.view.link)

(require 'conjure.view.form)

(require 'conjure.view.ajax)