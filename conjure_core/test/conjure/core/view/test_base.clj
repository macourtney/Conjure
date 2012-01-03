(ns conjure.core.view.test-base
  (:use clojure.test
        conjure.core.view.base)
  (require [clojure.string :as string]
           [com.reasonr.scriptjure :as scriptjure]
           [conjure.core.config.environment :as environment]
           [conjure.core.server.request :as request]))

(def-view [view-message]
  view-message)

(deftest test-defview
  (is (= "test" (render-body "test")))
  (request/with-controller-action "test" "test"
    (is (string? (render-str "test")))
    (is (map? (render-view "test")))))

(deftest test-link-to
  (is (= 
    [:a { :href "/hello/show" } "view"]
    (link-to "view" { :controller "hello" :action "show" })))
  (request/with-controller-action "hello" "add"
    (is (= 
      [:a { :href "/hello/show" } "view"]
      (link-to "view" { :action "show" }))))
  (is (= 
    [:a { :href "/hello/show", :id "foo", :class "bar" } "view"]
    (link-to "view" { :controller "hello" :action "show" :html-options { :id "foo" :class "bar" } })))
  (is (= 
    [:a { :href "/hello/show" } "show"]
    (link-to #(request/action) { :controller "hello" :action "show" })))
  (request/with-controller-action "hello" "show"
    (is (= 
      [:a { :href "/home/index" } "view"]
      (link-to "view" { :html-options { :href "/home/index" } } )))))

(deftest test-link-to-if
  (is (= 
    [:a { :href "/hello/show" } "view"]
    (link-to-if true "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-if false #(request/action) { :controller "hello" :action "show" })))
  (is (= 
    [:a { :href "/hello/show" } "view"]
    (link-to-if #(= (request/action) "show") "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if #(= (request/action) "add") "view" { :controller "hello" :action "show" }))))
  
(deftest test-link-to-unless
  (is (= "view" (link-to-unless true "view" { :controller "hello" :action "show" })))
  (is (= 
    [:a { :href "/hello/show" } "view"]
    (link-to-unless false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-unless true #(request/action) { :controller "hello" :action "show" })))
  (is (= "view" (link-to-unless #(= (request/action) "show") "view" { :controller "hello" :action "show" })))
  (is (= 
    [:a { :href "/hello/show" } "view"]
    (link-to-unless #(= (request/action) "add") "view" { :controller "hello" :action "show" }))))

(deftest test-form-for
  (is (= 
    [:form { :name "create", :action "/hello/create", :method "post" } "Blah"]
    (form-for { :name "create", :controller "hello", :action "create" } "Blah")))
  (is (= 
    [:form { :name "hello", :action "/hello/create", :method "post" } "Blah"]
    (form-for { :controller "hello", :action "create" } "Blah")))
  (is (= 
    [:form { :name "create", :action "/hello/create", :method "post" } "create"]
    (form-for { :name "create", :controller "hello", :action "create" } #(request/action))))
  (is (= 
    [:form { :name "create", :method "post", :action "/home/index" } "Blah"]
    (form-for { :name "create", :controller "hello", :action "create", :html-options { :action "/home/index" } } "Blah"))))

(deftest test-text-field
  (is (= 
    [:input { :type "text", :id "message-text", :name "message[text]", :value "Blah" }]
    (text-field { :text "Blah" } :message :text )))
  (is (= 
    [:input { :size 20, :type "text", :id "message-text", :name "message[text]", :value "Blah" }]
    (text-field { :text "Blah" } :message :text { :size 20 } ))))
  
(deftest test-text-area
  (is (= 
    [:textarea { :name "message[text]", :id "message-text", :rows 40, :cols 20 } "Blah"]
    (text-area { :text "Blah" } :message :text )))
  (is (= 
    [:textarea { :name "message[text]", :id "message-text", :rows 60, :cols 40 } "Blah"]
    (text-area { :text "Blah" } :message :text { :rows 60, :cols 40 } ))))

(deftest test-hidden-field
  (is (= 
    [:input { :type "hidden", :id "message-text", :name "message[text]", :value "Blah" }]
    (hidden-field { :text "Blah" } :message :text )))
  (is (= 
    [:input { :class "hidden-message", :type "hidden", :id "message-text", :name "message[text]", :value "Blah" }]
    (hidden-field { :text "Blah" } :message :text { :class "hidden-message" } ))))

(defn 
  expected-option-tag
  ([name] (expected-option-tag name name))
  ([name value] [:option { :value value } name])
  ([name value selected]
    [:option { :selected selected, :value value } name]))

(deftest test-option-tag
  (is (= 
    (expected-option-tag "test" "blah")
    (option-tag "test" "blah" false)))
  (is (= 
    (expected-option-tag "test" "blah" "true")
    (option-tag "test" "blah" true)))
  (is (= 
    (expected-option-tag "test" "blah")
    (option-tag { :name :test :value "blah" })))
  (is (= 
    (expected-option-tag "test" "blah" "true")
    (option-tag { :name :test :value "blah", :selected true })))
  (is (= 
    (expected-option-tag "test")
    (option-tag { :name :test })))
  (is (= 
    (expected-option-tag "test")
    (option-tag { :name "test" })))
  (is (= 
    (expected-option-tag "test")
    (option-tag :test)))
  (is (= 
    (expected-option-tag "test")
    (option-tag "test")))) 

(deftest test-option-tags
  (is (= 
    [(expected-option-tag "test" "blah")]
    (option-tags [{ :name :test, :value "blah" }])))
  (is (= 
    [(expected-option-tag "test" "blah")]
    (option-tags [{ :name "test", :value "blah" }])))
  (is (= 
    [ (expected-option-tag "test" "blah" "true")
      (expected-option-tag "test2" "blah2") ]
    (option-tags 
      [ { :name :test, :value "blah", :selected true }, 
        { :name :test2, :value "blah2" }])))
  (is (= 
    [ (expected-option-tag "test" "test" "true")
      (expected-option-tag "test2") ]
    (option-tags [{ :name :test, :selected true }, :test2])))
  (is (= 
    [ (expected-option-tag "test1")
      (expected-option-tag "test2")
      (expected-option-tag "test3") ]
    (option-tags [:test1, :test2, :test3]))))

(defn
  expected-select-tag
  ([html-options]
    [:select html-options []])
  ([html-options & options]
    [:select html-options 
      (list* options)]))

(deftest test-select-tag
  (is (= (expected-select-tag nil) (select-tag {})))
  (is (=
    (expected-select-tag { :id "pony" })
    (select-tag { :html-options { :id "pony" } })))
  (is (=
    (expected-select-tag { :id "pony" }
      (expected-option-tag "test" "blah"))
    (select-tag 
      { :html-options { :id "pony" } 
        :options [{ :name :test, :value "blah" }] })))
  (is (= 
    (expected-select-tag { :name "foo[bar]" }
      (expected-option-tag "bar")
      (expected-option-tag "baz" "baz" "true")
      (expected-option-tag "boz"))
    (select-tag { :bar "baz" } :foo :bar
      { :options [:bar :baz :boz] })))
  (is (= 
    (expected-select-tag { :name "foo[bar]" }
      (expected-option-tag "bar")
      (expected-option-tag "baz" "baz" "true")
      (expected-option-tag "boz"))
    (select-tag { :bar "baz" } :foo :bar
      { :options
        [ { :name :bar }
          { :name :baz }
          { :name :boz } ] })))
  (is (= 
    (expected-select-tag { :name "foo[bar]" }
      (expected-option-tag "foobar" "bar")
      (expected-option-tag "foobaz" "baz" "true")
      (expected-option-tag "fooboz" "boz"))
    (select-tag { :bar "baz" } :foo :bar
      { :options
        [ { :name :foobar, :value "bar" }
          { :name :foobaz, :value "baz" }
          { :name :fooboz, :value "boz" } ] }))))

(deftest test-options-select-value
  (is (= [:bar { :name :baz :selected true, :value :baz } :boz]
         (options-select-value [:bar :baz :boz] "baz"))))

(deftest test-options-from-records
  (is (= 
    [ { :name "name1", :value "value1" } ] 
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }], 
        :name-key :name, 
        :value-key :value })))
  (is (= 
    [ { :name "name1", :value "value1" }, { :name "name2", :value "value2" } ]
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }, { :name "name2", :value "value2" }], 
        :name-key :name,
        :value-key :value })))
  (is (= 
    [ { :name "name1", :value "value1" },
      { :name "name2", :value "value2" },
      { :name "name3", :value "value3" } ]
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }, { :name "name2", :value "value2" }, { :name "name3", :value "value3" }], 
        :name-key :name
        :value-key :value })))
  (is (= 
    [ { :name "name1", :value "value1" } ]
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }],
        :name-key :name })))
  (is (= 
    [ { :name "name1", :value "value1" } ]
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }] })))
  (is (= 
    [ { :name "", :value "" },
      { :name "name1", :value "value1" } ] 
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }]
        :blank true })))
  (is (= 
    [ { :name "value1", :value "value1" } ]
    (options-from-records 
      { :records [{ :id "value1" }] }))))

(deftest test-image-path
  (is (= "/images/edit.png" (image-path "edit.png")))
  (is (= "/images/icons/edit.png" (image-path "icons/edit.png")))
  (is (= "/icons/edit.png" (image-path "/icons/edit.png")))
  (is (= "http://www.conjureapplication.com/img/edit.png" 
    (image-path "http://www.conjureapplication.com/img/edit.png"))))

(deftest test-image-tag
  (is (= [:img { :src "/images/icon.png" }] (image-tag "icon.png")))
  (is (= [:img { :src "/icons/icon.png" }] (image-tag "/icons/icon.png")))
  (is (= 
    [:img { :class "menu-icon", :src "/icons/icon.png" }]
    (image-tag "/icons/icon.png" { :class "menu-icon" }))))

(deftest test-stylesheet-path
  (is (= "/stylesheets/style.css" (stylesheet-path "style")))
  (is (= "/stylesheets/style.css" (stylesheet-path "style.css")))
  (is (= "/stylesheets/dir/style.css" (stylesheet-path "dir/style.css")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style.js"))))

(deftest test-stylesheet-link-tag
  (is (= 
    [:link { :href "/stylesheets/style.css", :media "screen", :rel "stylesheet", :type "text/css" }]
    (stylesheet-link-tag "style")))
  (is (= 
    [:link { :href "/stylesheets/style.css", :media "screen", :rel "stylesheet", :type "text/css" }]
    (stylesheet-link-tag "style.css")))
  (is (= 
    [:link { :href "http://www.conjureapplication.com/style.css", :media "screen", :rel "stylesheet", :type "text/css" }]
    (stylesheet-link-tag "http://www.conjureapplication.com/style.css")))
  (is (= 
    [:link { :href "/stylesheets/style.css", :media "all", :rel "stylesheet", :type "text/css" }]
    (stylesheet-link-tag "style.css" { :media "all" })))
  (is (= 
    [ [:link { :href "/stylesheets/random.styles.css", :media "screen", :rel "stylesheet", :type "text/css" }]
      [:link { :href "/css/stylish.css", :media "screen", :rel "stylesheet", :type "text/css" }]]
    (stylesheet-link-tag ["random.styles.css" "/css/stylish"]))))

(deftest test-javascript-path
  (is (= "/javascripts/xmlhr.js" (javascript-path "xmlhr")))
  (is (= "/javascripts/dir/xmlhr.js" (javascript-path "dir/xmlhr.js")))
  (is (= "/dir/xmlhr.js" (javascript-path "/dir/xmlhr")))
  (is (= "http://www.conjureapplication.com/js/xmlhr.js" 
    (javascript-path "http://www.conjureapplication.com/js/xmlhr")))
  (is (= "http://www.conjureapplication.com/js/xmlhr.js" 
    (javascript-path "http://www.conjureapplication.com/js/xmlhr.js"))))
    
(deftest test-javascript-include-tag
  (is (= 
    [:script { :src "/javascripts/xmlhr.js", :type "text/javascript" } ""]
    (javascript-include-tag "xmlhr")))
  (is (= 
    [:script { :src "/javascripts/xmlhr.js", :type "text/javascript" } ""]
    (javascript-include-tag "xmlhr.js")))
  (is (= 
    [ [:script { :src "/javascripts/common.js", :type "text/javascript" } ""]
      [:script { :src "/elsewhere/cools.js", :type "text/javascript" } ""]]
    (javascript-include-tag ["common.js", "/elsewhere/cools"])))
  (is (= 
    [:script { :src "http://www.conjureapplication.com/js/xmlhr.js", :type "text/javascript" } ""]
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr")))
  (is (= 
    [:script { :src "http://www.conjureapplication.com/js/xmlhr.js", :type "text/javascript" } ""]
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr.js"))))
    
(deftest test-jquery-include-tag
  (is (= 
    [:script { :src (str "/javascripts/" environment/jquery), :type "text/javascript" } ""]
    (jquery-include-tag))))

(deftest test-mail-to
  (is (= 
    [:a { :href "mailto:me@example.com" } "me@example.com"]
    (mail-to { :address "me@example.com" })))
  (is (= 
    [:a { :href "mailto:me@example.com" } "My email"]
    (mail-to { :address "me@example.com", :name "My email" })))
  (is (= 
    [:a { :class "email", :href "mailto:me@example.com" } "My email"]
    (mail-to { :address "me@example.com", :name "My email" :html-options { :class "email" }})))
  (is (= 
    [:a { :href "mailto:me@example.com" } "me at example.com"]
    (mail-to { :address "me@example.com", :replace-at " at " })))
  (is (= 
    [:a { :href "mailto:me@example.com" } "me@example dot com"]
    (mail-to { :address "me@example.com", :replace-dot " dot " })))
  (is (=
    [:a { :href "mailto:me@example.com" } "me at example dot com"]
    (mail-to { :address "me@example.com", :replace-at " at ", :replace-dot " dot " })))
  (is (= 
    [:a { :href "mailto:me@example.com?cc=you%40example.com" } "me@example.com"]
    (mail-to { :address "me@example.com", :cc "you@example.com" })))
  (is (=
    [:a { :href "mailto:me@example.com?subject=Yo%21&cc=you%40example.com" } "me@example.com"]
    (mail-to { :address "me@example.com", :cc "you@example.com", :subject "Yo!" })))
  (is (= 
    [:a { :href "mailto:me@example.com?body=Hey.&bcc=you%40example.com" } "me@example.com"]
    (mail-to { :address "me@example.com", :bcc "you@example.com", :body "Hey." }))))

(deftest test-check-box
  (is (= 
    [ [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "1" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "0" }]]
    (check-box { :good 0 } :puppy :good)))
  (is (= 
    [ [:input { :type "checkbox", :id "blah", :name "puppy[good]", :value "1" }]
      [:input { :type "hidden", :id "blah", :name "puppy[good]", :value "0" }]]
    (check-box { :good 0 } :puppy :good { :id "blah" })))
  (is (=
    [ [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "true" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "0" }]]
    (check-box { :good 0 } :puppy :good {} true)))
  (is (= 
    [ [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "true" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "false" }]]
    (check-box { :good 0 } :puppy :good {} true false))))

(deftest test-radio-button
  (is (= 
    [:input { :type "radio", :id "puppy-breed", :name "puppy[breed]", :value "great-dane" }]
    (radio-button { :breed "chihuahua" } :puppy :breed "great-dane")))
  (is (=
    [:input { :checked "checked", :type "radio", :id "puppy-breed", :name "puppy[breed]", :value "chihuahua" }]
    (radio-button { :breed "chihuahua" } :puppy :breed "chihuahua")))
  (is (=
    [:input { :type "radio", :id "dog-breed", :name "puppy[breed]", :value "great-dane" }]
    (radio-button { :breed "chihuahua" } :puppy :breed "great-dane" { :id "dog-breed" }))))

(deftest test-xml-header-tag
  (is (= "<?xml version=\"1.0\"?>" (xml-header-tag)))
  (is (= "<?xml version=\"2.0\"?>" (xml-header-tag { :version "2.0" })))
  (is (= "<?xml encoding=\"UTF-8\" version=\"1.0\"?>" (xml-header-tag { :encoding "UTF-8" }))))

(deftest test-html-doctype
  (is (html-doctype :html4.01-strict))
  (is (html-doctype :html4.01-transitional))
  (is (html-doctype :html4.01-frameset))
  (is (html-doctype :xhtml1.0-strict))
  (is (html-doctype :xhtml1.0-transitional))
  (is (html-doctype :xhtml1.0-frameset))
  (is (html-doctype :xhtml1.1))
  (is (= (html-doctype) (html-doctype :xhtml1.0-transitional))))

(deftest test-success-fn
  (is (= '(ajaxContent "#test-id") (success-fn "test-id")))
  (is (= '(ajaxContent "#test-id") (success-fn "test-id" :content)))
  (is (= '(ajaxReplace "#test-id") (success-fn "test-id" :replace)))
  (is (= '(ajaxBefore "#test-id") (success-fn "test-id" :before)))
  (is (= '(ajaxAfter "#test-id") (success-fn "test-id" :after)))
  (is (= '(ajaxTop "#test-id") (success-fn "test-id" :top)))
  (is (= '(ajaxBottom "#test-id") (success-fn "test-id" :bottom))))
  
(deftest test-error-fn
  (is (= 'ajaxError (error-fn))))

(defn javascript-value [value]
  (cond
    (nil? value) "null"
    (instance? String value) (str "\"" value "\"")
    true value))

(defn javascript-key-value-pair [value-map include-key]
  (str (name include-key) ": " (javascript-value (get value-map include-key))))

(defn javascript-map-string [value-map include-keys]
  (str "{"  (string/join ", " (map javascript-key-value-pair (repeat value-map) include-keys)) "}"))

(defn ajax-click-string [id value-map include-keys]
  (str "ajaxClick(\"" id "\", " (javascript-map-string value-map include-keys) ")"))

(defn javascript-tag [javascript-string]
  [:script { :type "text/javascript" } javascript-string])

(deftest test-ajax-link-to
  (let [ajax-map { :type "POST"
                   :url "/home/index"
                   :dataType "html"
                   :success 'successFunction
                   :error 'ajaxError
                   :confirm nil }
        include-keys [:type :url :dataType :success :error :confirm]
        script-tag (javascript-tag (ajax-click-string "#test-id" ajax-map include-keys))
        a-tag [:a { :href "#", :id "test-id"}
                "update"]
        link-to-options { :controller "home", 
                          :action "index", 
                          :update 'successFunction, 
                          :html-options { :id "test-id" } }]
    (is (= 
      [a-tag (javascript-tag (ajax-click-string "#test-id" ajax-map include-keys))]
      (ajax-link-to "update" link-to-options)))
    (is (= 
      [a-tag (javascript-tag (ajax-click-string "#test-id" (assoc ajax-map :type "GET") include-keys))]
      (ajax-link-to "update" (assoc link-to-options :method "GET"))))
    (is (= 
      [a-tag (javascript-tag (ajax-click-string "#test-id" (assoc ajax-map :url "/hello/show") include-keys))]
      (ajax-link-to "update" (assoc link-to-options :ajax-url "/hello/show"))))
    (is (= 
      [a-tag (javascript-tag (ajax-click-string "#test-id" ajax-map include-keys))]
      (ajax-link-to "update" (assoc link-to-options :update { :success 'successFunction }))))
    (is (= 
      [ [:a { :id "test-id", :href "/noscript/update" }
          "update"]
       (javascript-tag (ajax-click-string "#test-id" (assoc ajax-map :error 'errorFunction) include-keys))]
      (ajax-link-to "update" 
        (merge 
          link-to-options 
          { :update { :success 'successFunction, 
                      :error 'errorFunction }
            :html-options { :id "test-id", 
                            :href "/noscript/update" } }))))))

(defn ajax-submit-string [id value-map include-keys]
  (str "ajaxSubmit(\"" id "\", " (javascript-map-string value-map include-keys) ")"))

(deftest test-ajax-form-for
  (let [form-map { :name "home", :action "/home/index", :method "post", :id "test-id" }
        form-tag [:form form-map
                   [:button { :name "button", :value "Submit", :type "submit" } "Submit"]]
        ajax-map { :type "POST"
                   :url "/home/index"
                   :dataType "html"
                   :success 'successFunction
                   :error 'ajaxError
                   :confirm nil }
        include-keys [:type :url :dataType :success :error :confirm]
        form-for-options { :controller "home", 
                           :action "index", 
                           :update 'successFunction, 
                           :html-options { :id "test-id" } }
        form-for-body (form-button "Submit")]
    (is (=
      [ form-tag (javascript-tag (ajax-submit-string "#test-id" ajax-map include-keys))]
      (ajax-form-for form-for-options form-for-body)))
    (is (=
      [ form-tag (javascript-tag (ajax-submit-string "#test-id" (assoc ajax-map :type "GET") include-keys))]
      (ajax-form-for 
        (assoc form-for-options :method "GET")
        form-for-body)))
    (is (=
      [ form-tag (javascript-tag (ajax-submit-string "#test-id" (assoc ajax-map :url "/hello/show") include-keys))]
      (ajax-form-for (assoc form-for-options :ajax-url "/hello/show") form-for-body)))
    (is (=
      [ [:form { :name "noscript-update", :method "post", :id "test-id", :action "/noscript/update" }
          [:button { :name "button", :value "Submit", :type "submit" } "Submit"]]
        (javascript-tag (ajax-submit-string "#test-id" (assoc ajax-map :error 'errorFunction) include-keys))]
      (ajax-form-for 
        (merge form-for-options 
          { :name "noscript-update"
            :update { :success 'successFunction, 
                      :error 'errorFunction }
            :html-options { :id "test-id", 
                            :action "/noscript/update" } })
        form-for-body)))))