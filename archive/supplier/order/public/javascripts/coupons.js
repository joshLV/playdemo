$(function () {
    $("#eCouponSn").focus(function () {
        $("#checksn").html("");
    });
    $("#verifyAmount").change(function () {
        verifyAmount();
    });
    $("#verifyAmount").blur(function () {
        verifyAmount();
    });
    $("#verifyAmount").keyup(function () {
        verifyAmount();
    });
    $("#verifyAmount").keydown(function () {
        verifyAmount();
    });
    $("#query").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }

    });
    $("#sure").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }
        $("#form").attr("target", "_self");
        $("#form").attr("action", "/coupons/single-verify");
        $("#form").attr("method", "POST");
        $("#form").submit();
    });
    $("#multi-sure").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }
        $("#form").attr("target", "_self");
        $("#form").attr("action", "/coupons/multi-verify");
        $("#form").attr("method", "POST");
        $("#form").submit();
    });
});

function verifyAmount() {
    var verifyAmount = $("#verifyAmount").val();
    var amount = $("#amount").val() * 1;
    if (verifyAmount == "") {
        $("#sure_amount").html("");
        $("#multi-sure").attr("disabled", true);
    } else {
        verifyAmount = verifyAmount * 1;
        if (verifyAmount >= 0 && amount == verifyAmount) {
            $("#sure_amount").html("√").css("color", "#FD6F00");
            $("#multi-sure").attr("disabled", false);
        } else {
            $("#sure_amount").html("×").css("color", "#FD6F00");
            $("#multi-sure").attr("disabled", true);
        }
    }
}
