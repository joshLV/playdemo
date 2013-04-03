/**
 * For real/SupplierReturnEntries/index.html
 * User: sujie
 * Date: 4/2/13 7:40 PM
 */
function showReasonModal(goodsName, returnCount, entryId) {
    $("#goods_name").html(goodsName);
    $("#count").html(returnCount);
    $("#popup").show();
    $("#reasonForm").attr("action",  "/real/return-entries/" + entryId + "/received?x-http-method-override=PUT");
}
$(function () {
    $("#confirm").click(function () {
        var result = true;
        var reason = $("#reason").val().trim();

        if (reason == "") {
            $("#note_reason").addClass("error");
            $("#note_reason").html("请输入未收到货原因!");
            $("#reason").focus();
            result = false;
        }
        if (result) {
            $("#reasonForm").submit();
        }
    });
});
