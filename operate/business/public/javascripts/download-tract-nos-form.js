$("#unDownloaded").click(function () {
    if (this.checked) {
        $("#unDownloaded").val(true)
    } else {
        $("#unDownloaded").val(false)
    }
});

function formSubmit() {
    $("#channel_shipping_form").attr("method", "get");
    $("#channel_shipping_form").attr("action", "/real/partner/download-track-nos/download");
    $("#channel_shipping_form").submit();
}


function formSingleSubmit(outerGoodsNo) {
    $("#channel_shipping_form").attr("method", "get");
    $("#channel_shipping_form").attr("action", "/real/partner/download-track-nos/download/" + outerGoodsNo);
    $("#channel_shipping_form").submit();
}

function formSearchSubmit() {
    $("#channel_shipping_form").attr("method", "get");
    $("#channel_shipping_form").attr("action", "/real/partner/download-track-nos");
    $("#channel_shipping_form").submit();
}

