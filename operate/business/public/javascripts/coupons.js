$(function () {
    var checkForm = function () {
        var supplierName = $("#supplierName").val();
        if (supplierName == "") {
            $("#err-supplierUser_supplier_id").html("请选择商户!");
            $("#supplierName").focus();
            return false;
        }
        var shopId = $("#supplierUser_shop_id").val();
        if (shopId == "") {
            $("#err-supplierUser_shop").html("请选择门店!");
            $("#supplierUser_shop_id").focus();
            return false;
        }
        var eCouponSn = $("#eCouponSn").val();
        if (eCouponSn == "") {
            $("#err-eCouponSn").html("请输入券号!");
            $("#eCouponSn").focus();
            return false;
        }
        var consumedAt = $("#consumedAt").val();
        if (!$("#consumedAt").is(":hidden") && consumedAt == "") {
            $("#err-consumedAt").html("请选择实际消费时间!");
            $("#consumedAt").focus();
            return false;
        }
        var remark = $("#remark").val();
        if (!$("#remark").is(":hidden") && remark == "") {
            $("#err-remark").html("请选择代理验证原因!");
            return false;
        }
        $("#shopId").val(shopId);
        return true;
    };

    $("#eCouponSn").focus(function () {
        $("#err-eCouponSn").html("");
    });

    $("#remarkSelect").focus(function() {
        $("#err-remark").html("");
    });

    $("#remark").focus(function() {
        $("#err-remark").html("");
    });

    $("#query").click(function () {
        if (!checkForm()) {
            return false;
        }
    });

    $("#sure").click(function () {
        if (!checkForm()) {
            return false;
        }
        $("#form").attr("target", "_self");
        $("#form").attr("action", "/coupons/verify");
        $("#form").attr("method", "POST");
        $("#form").submit();
    });

    $("#remarkSelect").change(function () {
        if ($("#remarkSelect").val() == "商家无法验证") {
            $("#remark").val("商家无法验证");
            $("#remark").hide();
        } else {
            $("#remark").val("");
            $("#remark").show();
        }
    })

});



