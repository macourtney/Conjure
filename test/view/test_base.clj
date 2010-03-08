(ns test.view.test-base
  (:use clj-html.core
        clojure.contrib.test-is
        conjure.view.base)
  (require [com.reasonr.scriptjure :as scriptjure]))

(defview [message]
  message)

(deftest test-defview
  (is (= "test" (render-view {} "test"))))

(deftest test-link-to
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to "view" { :controller "hello" :action "show" })))
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to "view" { :controller "hello" :action "add" } { :action "show" })))
  (is (= 
    (htmli [:a { :href "/hello/show", :id "foo", :class "bar" } "view"])
    (link-to "view" { :controller "hello" :action "show" :html-options { :id "foo" :class "bar" } })))
  (is (= 
    (htmli [:a { :href "/hello/show" } "show"])
    (link-to #(:action %) { :controller "hello" :action "show" })))
  (is (= 
    (htmli [:a { :href "/home/index" } "view"])
    (link-to "view" { :controller "hello" :action "show" } { :html-options { :href "/home/index" } } ))))

(deftest test-link-to-if
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to-if true "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-if false #(:action %) { :controller "hello" :action "show" })))
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to-if #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))
  
(deftest test-link-to-unless
  (is (= "view" (link-to-unless true "view" { :controller "hello" :action "show" })))
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to-unless false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-unless true #(:action %) { :controller "hello" :action "show" })))
  (is (= "view" (link-to-unless #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= 
    (htmli [:a { :href "/hello/show" } "view"])
    (link-to-unless #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))

(deftest test-form-for
  (is (= 
    (htmli [:form { :name "create", :action "/hello/create", :method "post" } "Blah"])
    (form-for { :name "create", :controller "hello", :action "create" } "Blah")))
  (is (= 
    (htmli [:form { :name "hello", :action "/hello/create", :method "post" } "Blah"])
    (form-for { :controller "hello", :action "create" } "Blah")))
  (is (= 
    (htmli [:form { :name "create", :action "/hello/create", :method "post" } "create"])
    (form-for { :name "create", :controller "hello", :action "create" } #(:action %))))
  (is (= 
    (htmli [:form { :name "create", :method "post", :action "/home/index" } "Blah"])
    (form-for { :name "create", :controller "hello", :action "create", :html-options { :action "/home/index" } } "Blah"))))

(deftest test-text-field
  (is (= 
    (htmli [:input { :type "text", :id "message-text", :name "message[text]", :value "Blah" }])
    (text-field { :text "Blah" } :message :text )))
  (is (= 
    (htmli [:input { :size 20, :type "text", :id "message-text", :name "message[text]", :value "Blah" }])
    (text-field { :text "Blah" } :message :text { :size 20 } ))))
  
(deftest test-text-area
  (is (= 
    (htmli [:textarea { :name "message[text]", :id "message-text", :rows 40, :cols 20 } "Blah"])
    (text-area { :text "Blah" } :message :text )))
  (is (= 
    (htmli [:textarea { :name "message[text]", :id "message-text", :rows 60, :cols 40 } "Blah"])
    (text-area { :text "Blah" } :message :text { :rows 60, :cols 40 } ))))

(deftest test-hidden-field
  (is (= 
    (htmli [:input { :type "hidden", :id "message-text", :name "message[text]", :value "Blah" }])
    (hidden-field { :text "Blah" } :message :text )))
  (is (= 
    (htmli [:input { :class "hidden-message", :type "hidden", :id "message-text", :name "message[text]", :value "Blah" }])
    (hidden-field { :text "Blah" } :message :text { :class "hidden-message" } ))))

(deftest test-option-tag
  (is (= 
    (htmli [:option { :value "blah" } "test"])
    (option-tag "test" "blah" false)))
  (is (= 
    (htmli [:option { :selected "true", :value "blah" } "test"])
    (option-tag "test" "blah" true)))
  (is (= 
    (htmli [:option { :value "blah" } "test"])
    (option-tag :test { :value "blah" })))
  (is (= 
    (htmli [:option { :selected "true", :value "blah" } "test"])
    (option-tag :test { :value "blah", :selected true })))
  (is (= 
    (htmli [:option { :value "test" } "test"])
    (option-tag :test {})))) 

(deftest test-option-tags
  (is (= 
    (htmli [:option { :value "blah" } "test"])
    (option-tags { :test { :value "blah" }})))
  (is (= 
    (htmli [:option { :value "blah" } "test"])
    (option-tags { "test" { :value "blah" }})))
  (is (= 
    (htmli 
      [:option { :selected "true", :value "blah" } "test"]
      [:option { :value "blah2" } "test2"])
    (option-tags { :test { :value "blah" :selected true }, :test2 { :value "blah2" }})))
  (is (= 
    (htmli 
      [:option { :selected "true", :value "test" } "test"]
      [:option { :value "test2" } "test2"])
    (option-tags { :test { :selected true }, :test2 nil })))
  (is (= 
    (htmli 
      [:option { :value "test1" } "test1"]
      [:option { :value "test2" } "test2"]
      [:option { :value "test3" } "test3"])
    (option-tags { :test1 nil, :test2 nil, :test3 nil }))))

(deftest test-select-tag
  (is (= (htmli [:select]) (select-tag {})))
  (is (= (htmli [:select { :id "pony" } ""]) (select-tag { :html-options { :id "pony" } })))
  (is (= 
    (htmli 
      [:select { :id "pony" } 
        [:option { :value "blah" } "test"]])
    (select-tag { :html-options { :id "pony" } :option-map { :test { :value "blah" }} })))
  (is (= 
    (htmli 
      [:select { :name "foo[bar]" } 
        [:option { :value "bar" } "bar"]
        [:option { :selected "true", :value "baz" } "baz"]
        [:option { :value "boz" } "boz"]])
    (select-tag { :bar "baz" } :foo :bar
      { :option-map 
        { :bar nil
          :baz nil
          :boz nil } })))
  (is (= 
    (htmli 
      [:select { :name "foo[bar]" } 
        [:option { :value "bar" } "bar"]
        [:option { :selected "true", :value "baz" } "baz"]
        [:option { :value "boz" } "boz"]])
    (select-tag { :bar "baz" } :foo :bar
      { :option-map 
        { :bar "bar"
          :baz "baz"
          :boz "boz" } })))
  (is (= 
    (htmli 
      [:select { :name "foo[bar]" } 
        [:option { :value "bar" } "bar"]
        [:option { :selected "true", :value "baz" } "baz"]
        [:option { :value "boz" } "boz"]])
    (select-tag { :bar "baz" } :foo :bar
      { :option-map 
        { :bar { :value "bar" }
          :baz { :value "baz" }
          :boz { :value "boz" } } }))))
          
(deftest test-option-map-select-value
  (is (= { :bar nil
           :baz { :selected true, :value "baz" }
           :boz nil }
         (option-map-select-value { :bar nil
             :baz nil
             :boz nil } "baz"))))

(deftest test-options-from-records
  (is (= 
    { "name1" { :value "value1" } } 
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }], 
        :name-key :name, 
        :value-key :value })))
  (is (= 
    { "name1" { :value "value1" }, "name2" { :value "value2" } } 
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }, { :name "name2", :value "value2" }], 
        :name-key :name,
        :value-key :value })))
  (is (= 
    { "name1" { :value "value1" }, "name2" { :value "value2" }, "name3" { :value "value3" } } 
    (options-from-records 
      { :records [{ :name "name1", :value "value1" }, { :name "name2", :value "value2" }, { :name "name3", :value "value3" }], 
        :name-key :name
        :value-key :value })))
  (is (= 
    { "name1" { :value "value1" } } 
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }],
        :name-key :name })))
  (is (= 
    { "name1" { :value "value1" } } 
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }] })))
  (is (= 
    { "name1" { :value "value1" } 
      "" { :value "" } } 
    (options-from-records 
      { :records [{ :name "name1", :id "value1" }]
        :blank true })))
  (is (= 
    { "value1" { :value "value1" } } 
    (options-from-records 
      { :records [{ :id "value1" }] }))))

(deftest test-image-path
  (is (= "/images/edit.png" (image-path "edit.png")))
  (is (= "/images/icons/edit.png" (image-path "icons/edit.png")))
  (is (= "/icons/edit.png" (image-path "/icons/edit.png")))
  (is (= "http://www.conjureapplication.com/img/edit.png" 
    (image-path "http://www.conjureapplication.com/img/edit.png"))))

(deftest test-image-tag
  (is (= (htmli [:img { :src "/images/icon.png" }]) (image-tag "icon.png")))
  (is (= (htmli [:img { :src "/icons/icon.png" }]) (image-tag "/icons/icon.png")))
  (is (= 
    (htmli [:img { :class "menu-icon", :src "/icons/icon.png" }]) 
    (image-tag "/icons/icon.png" { :class "menu-icon" }))))

(deftest test-stylesheet-path
  (is (= "/stylesheets/style.css" (stylesheet-path "style")))
  (is (= "/stylesheets/style.css" (stylesheet-path "style.css")))
  (is (= "/stylesheets/dir/style.css" (stylesheet-path "dir/style.css")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style.js"))))

(deftest test-stylesheet-link-tag
  (is (= 
    (htmli [:link { :href "/stylesheets/style.css", :media "screen", :rel "stylesheet", :type "text/css" }])
    (stylesheet-link-tag "style")))
  (is (= 
    (htmli [:link { :href "/stylesheets/style.css", :media "screen", :rel "stylesheet", :type "text/css" }])
    (stylesheet-link-tag "style.css")))
  (is (= 
    (htmli [:link { :href "http://www.conjureapplication.com/style.css", :media "screen", :rel "stylesheet", :type "text/css" }])
    (stylesheet-link-tag "http://www.conjureapplication.com/style.css")))
  (is (= 
    (htmli [:link { :href "/stylesheets/style.css", :media "all", :rel "stylesheet", :type "text/css" }])
    (stylesheet-link-tag "style.css" { :media "all" })))
  (is (= 
    (htmli 
      [:link { :href "/stylesheets/random.styles.css", :media "screen", :rel "stylesheet", :type "text/css" }]
      [:link { :href "/css/stylish.css", :media "screen", :rel "stylesheet", :type "text/css" }]) 
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
    (htmli [:script { :src "/javascripts/xmlhr.js", :type "text/javascript" } ""])
    (javascript-include-tag "xmlhr")))
  (is (= 
    (htmli [:script { :src "/javascripts/xmlhr.js", :type "text/javascript" } ""])
    (javascript-include-tag "xmlhr.js")))
  (is (= 
    (htmli 
      [:script { :src "/javascripts/common.js", :type "text/javascript" } ""]
      [:script { :src "/elsewhere/cools.js", :type "text/javascript" } ""])
    (javascript-include-tag ["common.js", "/elsewhere/cools"])))
  (is (= 
    (htmli [:script { :src "http://www.conjureapplication.com/js/xmlhr.js", :type "text/javascript" } ""])
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr")))
  (is (= 
    (htmli [:script { :src "http://www.conjureapplication.com/js/xmlhr.js", :type "text/javascript" } ""])
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr.js"))))
    
(deftest test-jquery-include-tag
  (is (= 
    (htmli [:script { :src (str "/javascripts/" environment/jquery), :type "text/javascript" } ""])
    (jquery-include-tag))))

(deftest test-mail-to
  (is (= 
    (htmli [:a { :href "mailto:me@example.com" } "me@example.com"])
    (mail-to { :address "me@example.com" })))
  (is (= 
    (htmli [:a { :href "mailto:me@example.com" } "My email"])
    (mail-to { :address "me@example.com", :name "My email" })))
  (is (= 
    (htmli [:a { :class "email", :href "mailto:me@example.com" } "My email"])
    (mail-to { :address "me@example.com", :name "My email" :html-options { :class "email" }})))
  (is (= 
    (htmli [:a { :href "mailto:me@example.com" } "me at example.com"])
    (mail-to { :address "me@example.com", :replace-at " at " })))
  (is (= 
    (htmli [:a { :href "mailto:me@example.com" } "me@example dot com"])
    (mail-to { :address "me@example.com", :replace-dot " dot " })))
  (is (=
    (htmli [:a { :href "mailto:me@example.com" } "me at example dot com"])
    (mail-to { :address "me@example.com", :replace-at " at ", :replace-dot " dot " })))
  (is (= 
    (htmli [:a { :href "mailto:me@example.com?cc=you%40example.com" } "me@example.com"])
    (mail-to { :address "me@example.com", :cc "you@example.com" })))
  (is (=
    (htmli [:a { :href "mailto:me@example.com?subject=Yo%21&cc=you%40example.com" } "me@example.com"])
    (mail-to { :address "me@example.com", :cc "you@example.com", :subject "Yo!" })))
  (is (= 
    (htmli [:a { :href "mailto:me@example.com?body=Hey.&bcc=you%40example.com" } "me@example.com"])
    (mail-to { :address "me@example.com", :bcc "you@example.com", :body "Hey." }))))

(deftest test-check-box
  (is (= 
    (htmli 
      [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "1" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "0" }])
    (check-box { :good 0 } :puppy :good)))
  (is (= 
    (htmli 
      [:input { :type "checkbox", :id "blah", :name "puppy[good]", :value "1" }]
      [:input { :type "hidden", :id "blah", :name "puppy[good]", :value "0" }])
    (check-box { :good 0 } :puppy :good { :id "blah" })))
  (is (=
    (htmli 
      [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "true" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "0" }])
    (check-box { :good 0 } :puppy :good {} true)))
  (is (= 
    (htmli 
      [:input { :type "checkbox", :id "puppy-good", :name "puppy[good]", :value "true" }]
      [:input { :type "hidden", :id "puppy-good", :name "puppy[good]", :value "false" }])
    (check-box { :good 0 } :puppy :good {} true false))))

(deftest test-radio-button
  (is (= 
    (htmli [:input { :type "radio", :id "puppy-breed", :name "puppy[breed]", :value "great-dane" }])
    (radio-button { :breed "chihuahua" } :puppy :breed "great-dane")))
  (is (=
    (htmli [:input { :checked "checked", :type "radio", :id "puppy-breed", :name "puppy[breed]", :value "chihuahua" }])
    (radio-button { :breed "chihuahua" } :puppy :breed "chihuahua")))
  (is (=
    (htmli [:input { :type "radio", :id "dog-breed", :name "puppy[breed]", :value "great-dane" }])  
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

(deftest test-ajax-link-to
  (let [ajax-map { :type "POST"
                   :url "/home/index"
                   :dataType "html"
                   :success 'successFunction
                   :error 'ajaxError
                   :confirm nil }
        script-tag [:script { :type "text/javascript" }
                     (scriptjure/js 
                       (ajaxClick "#test-id" (clj ajax-map)))]
        a-tag [:a { :href "#", :id "test-id"}
                "update"]
        link-to-options { :controller "home", 
                          :action "index", 
                          :update 'successFunction, 
                          :html-options { :id "test-id" } }]
    (is (= 
      (htmli a-tag script-tag)
      (ajax-link-to "update" link-to-options)))
    (is (= 
      (htmli a-tag
        [:script { :type "text/javascript" }
          (scriptjure/js 
            (ajaxClick "#test-id" (clj (assoc ajax-map :type "GET"))))])
      (ajax-link-to "update" (assoc link-to-options :method "GET"))))
    (is (= 
      (htmli a-tag
        [:script { :type "text/javascript" }
          (scriptjure/js 
            (ajaxClick "#test-id" (clj (assoc ajax-map :url "/hello/show"))))])
      (ajax-link-to "update" (assoc link-to-options :ajax-url "/hello/show"))))
    (is (= 
      (htmli a-tag script-tag)
      (ajax-link-to "update" (assoc link-to-options :update { :success 'successFunction }))))
    (is (= 
      (htmli 
        [:a { :id "test-id", :href "/noscript/update" }
          "update"]
        [:script { :type "text/javascript" }
          (scriptjure/js 
            (ajaxClick "#test-id" (clj (assoc ajax-map :error 'errorFunction))))])
      (ajax-link-to "update" 
        (merge 
          link-to-options 
          { :update { :success 'successFunction, 
                      :error 'errorFunction }
            :html-options { :id "test-id", 
                            :href "/noscript/update" } }))))))

(deftest test-ajax-form-for
  (let [form-map { :name "home", :action "/home/index", :method "post", :id "test-id" }
        form-tag [:form form-map
                   [:input { :name "button", :value "Submit", :type "submit" } ]]
        ajax-map { :type "POST"
                   :url "/home/index"
                   :dataType "html"
                   :success 'successFunction
                   :error 'ajaxError
                   :confirm nil }
        script-tag [:script { :type "text/javascript" }
                     (scriptjure/js 
                       (ajaxSubmit "#test-id" (clj ajax-map)))]
        form-for-options { :controller "home", 
                           :action "index", 
                           :update 'successFunction, 
                           :html-options { :id "test-id" } }
        form-for-body (form-button "Submit")]
    (is (=
      (htmli form-tag script-tag)
      (ajax-form-for form-for-options form-for-body)))
    (is (=
      (htmli form-tag 
        [:script { :type "text/javascript" }
         (scriptjure/js 
           (ajaxSubmit "#test-id" (clj (assoc ajax-map :type "GET"))))])
      (ajax-form-for (assoc form-for-options :method "GET") form-for-body)))
    (is (=
      (htmli form-tag 
        [:script { :type "text/javascript" }
         (scriptjure/js 
           (ajaxSubmit "#test-id" (clj (assoc ajax-map :url "/hello/show"))))])
      (ajax-form-for (assoc form-for-options :ajax-url "/hello/show") form-for-body)))
    (is (=
      (htmli 
        [:form { :name "noscript-update", :method "post", :id "test-id", :action "/noscript/update" }
          [:input { :name "button", :value "Submit", :type "submit" } ]]
        [:script { :type "text/javascript" }
         (scriptjure/js 
           (ajaxSubmit "#test-id" (clj (assoc ajax-map :error 'errorFunction))))])
      (ajax-form-for 
        (merge form-for-options 
          { :name "noscript-update"
            :update { :success 'successFunction, 
                      :error 'errorFunction }
            :html-options { :id "test-id", 
                            :action "/noscript/update" } })
        form-for-body)))))