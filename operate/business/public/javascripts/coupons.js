$(function () {
    $("#eCouponSn").focus(function () {
        $("#checksn").html("");
    });
    $("#query").click(function () {
        var shopId = $("#supplierUser_shop_id").val();
        if (shopId == "") {
            $("#checksn").html("请选择门店!");
            return false;
        }
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }
        $("#shopId").val(shopId);
    });
    $("#sure").click(function () {
        var shopId = $("#supplierUser_shop_id").val();
        if (shopId == "") {
            $("#checksn").html("请选择门店!");
            return false;
        }
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#checksn").html("请输入券号!");
            return false;
        }
        $("#shopId").val(shopId);
        $("#form").attr("target", "_self");
        $("#form").attr("action", "/coupons/verify");
        $("#form").attr("method", "POST");
        $("#form").submit();
    });
});



