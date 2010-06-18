(ns views.layouts.templates.header
  (:use conjure.core.view.base))

(def-view [title]
  [:div { :id "header" }

    ;; Logo
    [:h1 { :id "logo" } 
      (image-tag "conjure96white.png" { :alt "Conjure Bunny", :width "93", :height "86", :align "center" }) ;
      "&nbsp;"
      [:a { :href "./", :title (str title " [Go to homepage]") } title [:span]]]
    [:hr { :class "noscreen" }]          

    ;; Quick links
    [:div { :class "noscreen noprint" }
      [:p [:em "Quick links: " [:a { :href "#content" } "content"] "," [:a { :href "#tabs" } "navigation"] "," [:a { :href "#search" } "search" ] "."]]
      [:hr]]

    ;; Example Search
    ;[:div { :id "search", :class "noprint" }
    ;  [:form { :action "", :method "get" }
    ;    [:fieldset [:legend "Search"]
    ;      [:label 
    ;        [:span { :class "noscreen" } "Find:"]
    ;        [:span { :id="search-input-out" } [:input { :type "text", :name "", :id "search-input", :size "30" } ]]]
    ;      [:input { :type "image", :src "/images/search_submit.gif", :id "search-submit", :value "OK" }]]]]
  ])