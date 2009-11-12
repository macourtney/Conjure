(ns test.view.test-base
  (:use clojure.contrib.test-is
        conjure.view.base))

(defview [message]
  message)

(deftest test-defview
  (is (= "test" (render-view {} "test"))))

(deftest test-link-to
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "add" } { :action "show" })))
  (is (= "<a class=\"bar\" href=\"/hello/show\" id=\"foo\">view</a>" (link-to "view" { :controller "hello" :action "show" :html-options { :id "foo" :class "bar" } })))
  (is (= "<a href=\"/hello/show\">show</a>" (link-to #(:action %) { :controller "hello" :action "show" }))))

(deftest test-link-to-if
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-if true "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-if false #(:action %) { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-if #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))
  
(deftest test-link-to-unless
  (is (= "view" (link-to-unless true "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-unless false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-unless true #(:action %) { :controller "hello" :action "show" })))
  (is (= "view" (link-to-unless #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-unless #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))

(deftest test-form-for
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"create\">Blah</form>" (form-for { :name "create", :url { :controller "hello", :action "create" } } "Blah")))
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"hello\">Blah</form>" (form-for { :url { :controller "hello", :action "create" } } "Blah")))
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"create\">create</form>" (form-for { :name "create", :url { :controller "hello", :action "create" } } #(:action %)))))

(deftest test-text-field
  (is (= "<input id=\"message-text\" name=\"message[text]\" type=\"text\" value=\"Blah\" />" (text-field { :text "Blah" } :message :text )))
  (is (= "<input id=\"message-text\" name=\"message[text]\" size=\"20\" type=\"text\" value=\"Blah\" />" (text-field { :text "Blah" } :message :text { :size 20 } ))))
  
(deftest test-text-area
  (is (= "<textarea cols=\"20\" id=\"message-text\" name=\"message[text]\" rows=\"40\">Blah</textarea>" (text-area { :text "Blah" } :message :text )))
  (is (= "<textarea cols=\"40\" id=\"message-text\" name=\"message[text]\" rows=\"60\">Blah</textarea>" (text-area { :text "Blah" } :message :text { :rows 60, :cols 40 } ))))

(deftest test-hidden-field
  (is (= "<input id=\"message-text\" name=\"message[text]\" type=\"hidden\" value=\"Blah\" />" (hidden-field { :text "Blah" } :message :text )))
  (is (= "<input class=\"hidden-message\" id=\"message-text\" name=\"message[text]\" type=\"hidden\" value=\"Blah\" />" (hidden-field { :text "Blah" } :message :text { :class "hidden-message" } ))))

(deftest test-option-tag
  (is (= "<option value=\"blah\">test</option>" (option-tag "test" "blah" false)))
  (is (= "<option selected=\"true\" value=\"blah\">test</option>" (option-tag "test" "blah" true)))
  (is (= "<option value=\"blah\">test</option>" (option-tag :test { :value "blah" })))
  (is (= "<option selected=\"true\" value=\"blah\">test</option>" (option-tag :test { :value "blah" :selected true })))
  (is (= "<option value=\"test\">test</option>" (option-tag :test {})))) 

(deftest test-option-tags
  (is (= "<option value=\"blah\">test</option>" (option-tags { :test { :value "blah" }})))
  (is (= "<option value=\"blah\">test</option>" (option-tags { "test" { :value "blah" }})))
  (is (= "<option selected=\"true\" value=\"blah\">test</option><option value=\"blah2\">test2</option>" (option-tags { :test { :value "blah" :selected true }, :test2 { :value "blah2" }})))
  (is (= "<option selected=\"true\" value=\"test\">test</option><option value=\"test2\">test2</option>" (option-tags { :test { :selected true }, :test2 nil })))
  (is (= "<option value=\"test1\">test1</option><option value=\"test2\">test2</option><option value=\"test3\">test3</option>" (option-tags { :test1 nil, :test2 nil, :test3 nil }))))

(deftest test-select-tag
  (is (= "<select />" (select-tag {})))
  (is (= "<select id=\"pony\"></select>" (select-tag { :html-options { :id "pony" } })))
  (is (= 
    "<select id=\"pony\"><option value=\"blah\">test</option></select>" 
    (select-tag { :html-options { :id "pony" } :option-map { :test { :value "blah" }} })))
  (is (= 
    "<select name=\"foo[bar]\"><option value=\"bar\">bar</option><option selected=\"true\" value=\"baz\">baz</option><option value=\"boz\">boz</option></select>" 
    (select-tag { :bar "baz" } :foo :bar
      { :option-map 
        { :bar nil
          :baz nil
          :boz nil } }))))
          
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
      { :records [{ :name "name1", :id "value1" }] }))))

(deftest test-image-path
  (is (= "/images/edit.png" (image-path "edit.png")))
  (is (= "/images/icons/edit.png" (image-path "icons/edit.png")))
  (is (= "/icons/edit.png" (image-path "/icons/edit.png")))
  (is (= "http://www.conjureapplication.com/img/edit.png" 
    (image-path "http://www.conjureapplication.com/img/edit.png"))))

(deftest test-image-tag
  (is (= "<img src=\"/images/icon.png\" />" (image-tag "icon.png")))
  (is (= "<img src=\"/icons/icon.png\" />" (image-tag "/icons/icon.png")))
  (is (= "<img class=\"menu-icon\" src=\"/icons/icon.png\" />" (image-tag "/icons/icon.png" { :class "menu-icon" }))))

(deftest test-stylesheet-path
  (is (= "/stylesheets/style.css" (stylesheet-path "style")))
  (is (= "/stylesheets/style.css" (stylesheet-path "style.css")))
  (is (= "/stylesheets/dir/style.css" (stylesheet-path "dir/style.css")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style")))
  (is (= "http://www.conjureapplication.com/css/style.css" (stylesheet-path "http://www.conjureapplication.com/css/style.js"))))

(deftest test-stylesheet-link-tag
  (is (= 
    "<link href=\"/stylesheets/style.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" 
    (stylesheet-link-tag "style")))
  (is (= 
    "<link href=\"/stylesheets/style.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" 
    (stylesheet-link-tag "style.css")))
  (is (= 
    "<link href=\"http://www.conjureapplication.com/style.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" 
    (stylesheet-link-tag "http://www.conjureapplication.com/style.css")))
  (is (= 
    "<link href=\"/stylesheets/style.css\" media=\"all\" rel=\"stylesheet\" type=\"text/css\" />" 
    (stylesheet-link-tag "style.css" { :media "all" })))
  (is (= 
    "<link href=\"/stylesheets/random.styles.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" /><link href=\"/css/stylish.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />" 
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
  (is (= "<script src=\"/javascripts/xmlhr.js\" type=\"text/javascript\" />" (javascript-include-tag "xmlhr")))
  (is (= "<script src=\"/javascripts/xmlhr.js\" type=\"text/javascript\" />" (javascript-include-tag "xmlhr.js")))
  (is (= 
    "<script src=\"/javascripts/common.js\" type=\"text/javascript\" /><script src=\"/elsewhere/cools.js\" type=\"text/javascript\" />"
    (javascript-include-tag ["common.js", "/elsewhere/cools"])))
  (is (= 
    "<script src=\"http://www.conjureapplication.com/js/xmlhr.js\" type=\"text/javascript\" />"
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr")))
  (is (= 
    "<script src=\"http://www.conjureapplication.com/js/xmlhr.js\" type=\"text/javascript\" />"
    (javascript-include-tag "http://www.conjureapplication.com/js/xmlhr.js"))))

(deftest test-mail-to
  (is (= "<a href=\"mailto:me@example.com\">me@example.com</a>" (mail-to { :address "me@example.com" })))
  (is (= "<a href=\"mailto:me@example.com\">My email</a>" (mail-to { :address "me@example.com", :name "My email" })))
  (is (= 
    "<a class=\"email\" href=\"mailto:me@example.com\">My email</a>" 
    (mail-to { :address "me@example.com", :name "My email" :html-options { :class "email" }})))
  (is (= 
    "<a href=\"mailto:me@example.com\">me at example.com</a>" 
    (mail-to { :address "me@example.com", :replace-at " at " })))
  (is (= 
    "<a href=\"mailto:me@example.com\">me@example dot com</a>" 
    (mail-to { :address "me@example.com", :replace-dot " dot " })))
  (is (= 
    "<a href=\"mailto:me@example.com\">me at example dot com</a>" 
    (mail-to { :address "me@example.com", :replace-at " at ", :replace-dot " dot " })))
  (is (= 
    "<a href=\"mailto:me@example.com?cc=you%40example.com\">me@example.com</a>" 
    (mail-to { :address "me@example.com", :cc "you@example.com" })))
  (is (= 
    "<a href=\"mailto:me@example.com?subject=Yo%21&cc=you%40example.com\">me@example.com</a>" 
    (mail-to { :address "me@example.com", :cc "you@example.com", :subject "Yo!" })))
  (is (= 
    "<a href=\"mailto:me@example.com?body=Hey.&bcc=you%40example.com\">me@example.com</a>" 
    (mail-to { :address "me@example.com", :bcc "you@example.com", :body "Hey." }))))

(deftest test-check-box
  (is (= 
    "<input id=\"puppy-good\" name=\"puppy[good]\" type=\"checkbox\" value=\"1\" /><input id=\"puppy-good\" name=\"puppy[good]\" type=\"hidden\" value=\"0\" />"
    (check-box { :good 0 } :puppy :good)))
  (is (= 
    "<input id=\"blah\" name=\"puppy[good]\" type=\"checkbox\" value=\"1\" /><input id=\"blah\" name=\"puppy[good]\" type=\"hidden\" value=\"0\" />"
    (check-box { :good 0 } :puppy :good { :id "blah" })))
  (is (= 
    "<input id=\"puppy-good\" name=\"puppy[good]\" type=\"checkbox\" value=\"true\" /><input id=\"puppy-good\" name=\"puppy[good]\" type=\"hidden\" value=\"0\" />"
    (check-box { :good 0 } :puppy :good {} true)))
  (is (= 
    "<input id=\"puppy-good\" name=\"puppy[good]\" type=\"checkbox\" value=\"true\" /><input id=\"puppy-good\" name=\"puppy[good]\" type=\"hidden\" value=\"false\" />"
    (check-box { :good 0 } :puppy :good {} true false))))

(deftest test-radio-button
  (is (= 
    "<input id=\"puppy-breed\" name=\"puppy[breed]\" type=\"radio\" value=\"great-dane\" />" 
    (radio-button { :breed "chihuahua" } :puppy :breed "great-dane")))
  (is (= 
    "<input checked=\"checked\" id=\"puppy-breed\" name=\"puppy[breed]\" type=\"radio\" value=\"chihuahua\" />" 
    (radio-button { :breed "chihuahua" } :puppy :breed "chihuahua")))
  (is (= 
    "<input id=\"dog-breed\" name=\"puppy[breed]\" type=\"radio\" value=\"great-dane\" />" 
    (radio-button { :breed "chihuahua" } :puppy :breed "great-dane" { :id "dog-breed" }))))