/**
 * For real/ReturnEntries/index.html.
 * User: sujie
 * Date: 4/1/13 5:24 PM
 */
function showConfirmModal(goodsName, returnCount, entryId) {
    $("#return_goods_name").html(goodsName);
    $("#return_count").html(returnCount);
    $("#confirmModal").modal('show');
    $("#confirmForm").attr("action", "/real/return-entries/" + entryId + "/received?x-http-method-override=PUT");
}

function showReasonModal(goodsName, returnCount, entryId) {
    $("#goods_name").html(goodsName);
    $("#count").html(returnCount);
    $("#reasonModal").modal('show');
    $("#reasonForm").attr("action",  "/real/return-entries/" + entryId + "/unreceived?x-http-method-override=PUT");
}
$(function () {

    $("#receive").click(function () {
        console.log();
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
