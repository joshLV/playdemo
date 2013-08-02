/**
 * For real/ReturnEntries/index.html.
 * User: sujie
 * Date: 4/1/13 5:24 PM
 */
function showConfirmModal(goodsName, supplierId, returnCount, entryId) {
    $("#return_goods_name").html(goodsName);
    $("#return_count").html(returnCount);
    $("#maxReturnCount").html(returnCount);
    $("#stockInCount").val(returnCount);
    $("#confirmModal").modal('show');
    if (supplierId == 5) {
        $("#instock").show();
        $("unreceivedCount").show();
    }
    $("#confirmForm").attr("action", "/real/return-entries/" + entryId + "/received?x-http-method-override=PUT");
}

function showReasonModal(goodsName, partialRefundPrice, returnCount, entryId, salePrice) {
    $("#goods_name").html(goodsName);
    $("#count").html(returnCount);
    $("#reasonModal").modal('show');

    $("#refundAmount").html(partialRefundPrice);
    if (partialRefundPrice == '') {
        $("unreceivedCount").show();
        $("#unreceivedAmount").show();
        $("#number_refund_amount").text("按数量");
        $("#refundAmount").html(returnCount * salePrice + "元");
    } else {
        $("#unreceivedAmount").show();
    }
    $("#reasonForm").attr("action", "/real/return-entries/" + entryId + "/unreceived?x-http-method-override=PUT");
}
$(function () {

    $("#receive").click(function () {
        if ($("#stockInCount").val() == '') {

        }

        $("#confirmForm").submit();
    });
    $("#confirm").click(function () {
        var result = true;
        var reason = $("#unreceivedReason").val().trim();

        if (reason == "") {
            $("#note_unreceivedReason").addClass("error");
            $("#note_unreceivedReason").html("请输入未收到货原因!");
            $("#unreceivedReason").focus();
            result = false;
        }
        if (result) {
            $("#reasonForm").submit();
        }
    });
});
