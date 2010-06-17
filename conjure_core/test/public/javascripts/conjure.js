
/*
Called by Conjure's link-to-remote. ajaxOptions is exactly the same as jqueries' ajax function options.
*/
function ajaxClick(id, ajaxOptions) {
    $(document).ready(function () {
        $(id).click(function (e) {
            e.preventDefault();
            
            if ((ajaxOptions.confirm == null) || (ajaxOptions.confirm())) {
                $.ajax(ajaxOptions);
            }
        });
    });
}

/*
Used by ajaxSubmit to add the data from an element into the given data map.
*/
function updateDataCallBack(data) {
    return function(index, domElement) {
        var inputElement = $(domElement);
        
        data[inputElement.attr("name")] = inputElement.val();
    }
}

/*
Called by Conjure's remote-form-for. ajaxOptions is similar to jqueries ajax function options, but the data option is 
automatically generated from the values in the form with the given id.
*/
function ajaxSubmit(id, ajaxOptions) {
    $(document).ready(function () {
        $(id).submit(function () {

            var newOptions = ajaxOptions; 

            if (newOptions.data == null) {
                // Copy ajaxOptions before we try to change them.
                newOptions = new Object();
                for (var key in ajaxOptions) {
                    newOptions[key] = ajaxOptions[key];
                }
                
                var data = new Object();
             
                $(id).find("input[type!=submit],textarea,select").not("input[type=checkbox]").each(updateDataCallBack(data));
                
                $(id).find("input:checkbox:checked").each(updateDataCallBack(data));
                $(id).find("input:radio:checked").each(updateDataCallBack(data));
                
                newOptions.data = data;
            }

            $.ajax(newOptions);

            return false;
        });
    });
}

/*
A basic ajax success function for replacing the contents of the element with the given id with the results of an ajax 
request.
*/
function ajaxContent(id) {
    return function (data) {
        $(id).html(data);
    }
}

/*
A basic ajax success function for replacing the element with the given id with the results of an ajax 
request.
*/
function ajaxReplace(id) {
    return function (data) {
        $(id).replaceWith(data);
    }
}

/*
A basic ajax success function for adding the results of an ajax request before the element the given id.
*/
function ajaxBefore(id) {
    return function (data) {
        $(id).before(data);
    }
}

/*
A basic ajax success function for adding the results of an ajax request after the element the given id.
*/
function ajaxAfter(id) {
    return function (data) {
        $(id).after(data);
    }
}

/*
A basic ajax success function for adding the results of an ajax request as the first element in the element the given 
id.
*/
function ajaxTop(id) {
    return function (data) {
        $(id).prepend(data);
    }
}

/*
A basic ajax success function for adding the results of an ajax request as the last element in the element the given 
id.
*/
function ajaxBottom(id) {
    return function (data) {
        $(id).append(data);
    }
}

/*
A basic ajax success function for prepending the results of an ajax request to an element with the given id.
*/
function ajaxTop(id) {
    return function (data) {
        $(id).prepend(data);
    }
}

/*
A basic ajax success function for removing the element with the given id.
*/
function ajaxRemove(id) {
    return function (data) {
        $(id).remove();
    }
}

/*
A basic function for displaying an error message if the ajax request fails.
*/
function ajaxError(XMLHttpRequest, textStatus, errorThrown) {
    alert("Error! " + textStatus + "\n" + errorThrown);
}

/*
Returns a function which calls the confirm dialog with the given message.
*/
function ajaxConfirm(message) {
    return function () {
        return confirm(message);
    }
}

/*
Used on the list records page to initialize the 'Add' link for ajax.
*/
function initListAddLink(linkSelector, formSelector) {
    $(document).ready(function () {
        $(linkSelector).click(function (e) {
            e.preventDefault();
            
            $(formSelector).show();
            $(linkSelector).hide();
        });
    });
}

function addFormSuccess(tableSelector, linkSelector, formSelector) {
    return function (data) {
        $(tableSelector).append(data);
        
        $(formSelector).hide();
        $(linkSelector).show();
    }
}