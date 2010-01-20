function ajaxClick(id, ajaxOptions) {
    $(document).ready(function () {
        $(id).click(function (e) {
            e.preventDefault();
            
            $.ajax(ajaxOptions);
        });
    });
}

function ajaxSubmit(id, ajaxOptions) {
    $(document).ready(function () {
        $(id).submit(function () {
            $.ajax(ajaxOptions);

            return false;
        });
    });
}

function ajaxError(XMLHttpRequest, textStatus, errorThrown) {
    alert("Error! " + textStatus + "\n" + errorThrown);
}