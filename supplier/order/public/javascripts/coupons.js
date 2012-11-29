$(function () {
    $("#eCouponSn").focus(function () {
        $("#checksn").html("");
        $("#showinfo").html("");
        $("#statusw").html("")
    });
    $("#verifyAmount").change(function () {
        verifyAmount();
    });
    $("#verifyAmount").blur(function () {
        verifyAmount();
    });

    $("#query").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#shopId").val();
        var isSingle = $("#isSingle").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }

        $("#showinfo").load("/coupons/query?isSingle=" + isSingle + "&shopId=" + shopId + "&eCouponSn=" + eCouponSn.trim(), function (data) {
        });

    });
    $("#sure").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }
        $("#form").attr("target", "_self");
    });

});

function verifyAmount() {
    var amount = $("#verifyAmount").val();
    if (amount * 1 > 0) {
        $("#sure").attr("disabled", false);
    } else {
        $("#verifyAmount-err").html("请输入验证金额!");
        $("#sure").attr("disabled", true);
    }
}
