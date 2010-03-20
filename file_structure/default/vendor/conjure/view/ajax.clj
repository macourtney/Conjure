(ns conjure.view.ajax)

(in-ns 'conjure.view.base)

(require ['com.reasonr.scriptjure :as 'scriptjure])

(defn-
#^{ :doc "Returns the position function for the given position." }
  position-function [position]
  (cond 
    (= position :content) 'ajaxContent
    (= position :replace) 'ajaxReplace
    (= position :before) 'ajaxBefore
    (= position :after) 'ajaxAfter
    (= position :top) 'ajaxTop
    (= position :bottom) 'ajaxBottom
    (= position :remove) 'ajaxRemove))
    
(defn
#^{ :doc "Creates a link-to-remote-onclick success function which adds the returned content to the tag with the given 
id based on position. Position can be one of the following:

  :content - Replaces the entire contents of success-id (default)
  :replace - Replaces success-id
  :before - Adds the content before success-id
  :after - Adds the content after success-id
  :top - Adds the content in the first position in success-id
  :bottom - Adds the content in the last position in success-id
  :remove - Removes success-id" }
  success-fn 
  ([success-id] (success-fn success-id :content))
  ([success-id position]
    (scriptjure/quasiquote
      ((clj (position-function position)) (clj (str "#" success-id))))))

(defn
#^{ :doc "Creates a standard link-to-remote-onclick error function which simply displays the returned error." }
  error-fn []
  'ajaxError)

(defn
#^{ :doc "Creates a standard confirm dialog with the given message." }
  confirm-fn [message]
  (scriptjure/quasiquote (ajaxConfirm (clj message))))
  
(defn-
#^{ :doc "Generates the ajax map for the given params. Valid params are:

    :method - The method to use for the ajax call. Default is \"POST\"
    :ajax-url - The url for the ajax request to call instead of creating a url from the given controller and action.
    :update - A scriptjure function or a map. If it is a function, then it is called when the ajax request returns with 
              success. If it is a map, then the scriptjure function value of :success is called when the ajax request 
              returns successfully, and the scriptjure function value of :error is called when the ajax request fails.
    :confirm - A scriptjure function to call to confirm the action before the ajax call is executed." }
  ajax-map [request-map]
  (let [ajax-type (or (:method request-map) "POST")
        url (or (:ajax-url request-map) (view-utils/url-for request-map))
        update (:update request-map)
        success-fn (if (map? update) (:success update) update)
        error-fn (if (and (map? update) (contains? update :error)) (:error update) (error-fn))
        confirm-fn (:confirm request-map)]

    (scriptjure/quasiquote 
      { :type (clj ajax-type)
        :url (clj url)
        :dataType "html"
        :success (clj success-fn)
        :error (clj error-fn)
        :confirm (clj confirm-fn) })))

(defn 
#^{ :doc 
"Returns an ajax link for the given text and parameters using url-for. Params has the same valid parameters as url-for, 
plus:

     :update - A map or a scriptjure function. If the value is a function, then it is called when the ajax request 
               succeeds.
               If the value is a map then it looks for the following keys: 
                  :success - The id of the element to update if the request succeeds.
                  :failure - The id of the element to update if the request fails.
     :method - The request method. Possible values POST, GET, PUT, DELETE. However, not all browsers support PUT and 
               DELETE. Default is POST.
     :confirm - a method to call before the ajax call to get a confirmation from the user.
     :html-options - a map of html attributes to add to the anchor tag.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  ajax-link-to
  ([text request-map params] (ajax-link-to text (view-utils/merge-url-for-params request-map params)))
  ([text request-map]
    (let [html-options (or (:html-options request-map) {})
          id (or (:id html-options) (str "id-" (rand-int 1000000)))
          id-string (str "#" id)
          ajax-function (ajax-map request-map)]
      (list
        [:a 
          (merge html-options 
            { :href (or (:href html-options) "#")
              :id id })
          (evaluate-if-fn text request-map)]
        [:script { :type "text/javascript" } 
          (scriptjure/js
            (ajaxClick (clj id-string) (clj ajax-function)))]))))

(defn
#^{ :doc 
"Returns an ajax form for with the given body. Params has the same valid parameters as form-for, 
plus:

     :update - The id of the element to update. If the value is a map then it looks for the following keys: 
                  :success - The id of the element to update if the request succeeds.
                  :failure - The id of the element to update if the request fails.
     :method - The request method. Possible values POST, GET, PUT, DELETE. However, not all browsers support PUT and 
               DELETE. Default is POST.
     :confirm - a method to call before the ajax call to get a confirmation from the user.
     
If text is a function, then it is called passing params. If link-to is called with text a function and both request-map
and params, text is called with request-map and params merged (not all keys used from request-map)." }
  ajax-form-for
  ([request-map options body] 
    (ajax-form-for (view-utils/merge-url-for-params request-map options) body))
  ([request-map body]
    (let [html-options (or (:html-options request-map) {})
          id (or (:id html-options) (str "id-" (rand-int 1000000)))
          id-string (str "#" id)
          ajax-function (ajax-map request-map)
          form-for-options (assoc request-map :html-options (merge html-options { :id id }))]
      (list
        (form-for form-for-options body)
        [:script { :type "text/javascript" } 
          (scriptjure/js
            (ajaxSubmit (clj id-string) (clj ajax-function)))]))))