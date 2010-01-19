function ajaxClick(id, ajaxOptions) {
    $(document).ready(function () {
        $(id).click(function (e) {
            e.preventDefault();
            
            $.ajax(ajaxOptions);
        });
    });
}

function ajaxError(XMLHttpRequest, textStatus, errorThrown) {
    alert("Error! " + textStatus + "\n" + errorThrown);
}