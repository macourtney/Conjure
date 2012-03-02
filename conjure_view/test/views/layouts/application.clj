(ns views.layouts.application
  (:use conjure.core.view.base)
  (:require [com.reasonr.scriptjure :as scriptjure]
            [views.layouts.templates.breadcrumbs :as breadcrumbs]
            [views.layouts.templates.header :as header]
            [views.layouts.templates.links-sidebar :as links-sidebar]
            [views.layouts.templates.tabs :as tabs]))

(def-layout [body]
  (let [title "Conjure"]
    (xml-header-tag)
    (html-doctype)
    [:html { :xmlns "http://www.w3.org/1999/xhtml", :xml:lang "en", :lang "en" }
      [:head
        [:meta { :http-equiv "content-type", :content "text/html; charset=utf-8" } ]
        [:meta { :http-equiv "content-language", :content "en" } ]
        [:meta { :name "copyright", :content "Design/Code: Vit Dlouhy [Nuvio - www.nuvio.cz]; e-mail: vit.dlouhy@nuvio.cz" } ]

        [:title title]
        [:meta { :name "description", :content "..." } ]
        [:meta { :name="keywords", :content "..." } ]

        [:link { :rel "icon", :type "image/vnd.microsoft.icon" :href (image-path "favicon.ico") } ]
        [:link { :rel "index", :href "./", :title "Home" } ]
        (stylesheet-link-tag "main.css" { :media "screen,projection" } )
        (stylesheet-link-tag "print.css" { :media "print" } )
        (stylesheet-link-tag "aural.css" { :media "aural" } )
        (jquery-include-tag)
        (conjure-js-include-tag)]

      [:body { :id "www-url-cz" }

        ;; Main
        [:div { :id "main", :class "box" }

          ;; Header
          (header/render-body title)

          ;; Main menu (tabs)
          (tabs/render-body)

          ;; Page (2 columns)
          [:div { :id "page", :class="box" }
            [:div { :id "page-in", :class "box" }

              [:div { :id "strip" :class "box noprint" }

                ;; Example RSS feeds -->
                ;[:p { :id "rss" } [:strong "RSS:"] [:a { :href "#" } "articles"] " / " [:a { :href "#" } "comments"]]
                ;[:hr { :class "noscreen" }]

                ;; Breadcrumbs
                (breadcrumbs/render-body)]

              ;; Content
              [:div { :id "content" }
                  body

                  ;; Example Article:
                  ;;[:div { :class "article" }
                  ;;  [:h2 [:span [:a { :href "#" } "This is my best article"]]]
                  ;;  [:p class="info noprint">
                  ;;    [:span { :class "date" } "2007-01-01 @ 00:01"] [:span { :class "noscreen" } ","]
                  ;;    [:span { :class "cat" } [:a { :href "#" } "Category"]] [:span { :class "noscreen" } ","]
                  ;;    [:span { :class "user" } [:a { :href "#" } "My name"]] [:span { :class "noscreen" } ","]
                  ;;    [:span { :class "comments" } [:a { :href "#" } "Comments"]]]
                  ;;
                  ;;  [:p "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aliquam pellentesque enim blandit enim bibendum blandit.
                  ;;  Integer eu leo ac est aliquet imperdiet. Quisque nec justo id augue posuere malesuada. Nullam ac metus. Cras non leo
                  ;;  ut est placerat condimentum. Aliquam ut enim. Quisque non sapien in enim eleifend faucibus. Pellentesque sodales. Mauris
                  ;;  auctor arcu sit amet felis. Donec eget enim ut lacus pharetra condimentum. Nulla in felis vel tortor imperdiet consectetuer.
                  ;;  Sed id ante."]
                  ;;
                  ;;  [:p { :class "btn-more box noprint" } [:strong [:a { :href "#" } "Continue"]]]]
                  ;;
                  ;;[:hr { :class "noscreen" }
              ] ; content end

              ;; Right column
              [:div { :id "col", :class"noprint" }
                [:div { :id "col-in" }

                  ;; Example About Me
                  ;[:h3 [:span [:a { :href "#" } "About Me"]]]

                  ;[:div { :id "about-me" }
                  ;  [:p (image-tag "tmp_photo.gif" { :id "me", :alt "Yeah, it´s me!" } )
                  ;  [:strong "John Doe"] [:br]
                  ;  "Age: 26" [:br]
                  ;  "Dallas, TX" [:br]
                  ;  [:a { :href "#" } "Profile on MySpace"]]]

                  ;[:hr { :class "noscreen" }]

                  ;; Links Sidebar
                  (links-sidebar/render-body)]]]]

            ;; Footer
          [:div { :id "footer" }
            [:div { :id "top", :class "noprint" } [:p [:span { :class "noscreen" } "Back on top"] [:a { :href "#header", :title "Back on top ^" } "^"[:span]]]]
            [:hr { :class "noscreen" }]
        
            [:p { :id "createdby" } "created by " [:a { :href "http://www.nuvio.cz" } "Nuvio | Webdesign"]] ; DON´T REMOVE, PLEASE!
            [:p { :id "copyright" } "&copy; 2007 " [:a { :href "mailto:my@mail.com" } "My Name"]]]]]

      [:script { :type "text/javascript" } 
        (scriptjure/js
          (.ready ($ document) (fn [] 
            (.hide ($ "#add-action-link")))))]]))