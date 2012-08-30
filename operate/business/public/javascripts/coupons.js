$(function () {
    $("#eCouponSn").focus(function () {
        $("#checksn").html("");
        $("#showinfo").html("");
        $("#statusw").html("");
    });

    $("#query").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#supplierUser_shop_id").val();
        var supplierId = $("#supplierId").val();

        if (eCouponSn == "") {
            $("#checksn").html("<font color=red>请输入券号!</font>");
            return false;
        }
        var shopName = $("#shopName").val();
        if (shopName == undefined) {
            shopName = $("#shopId option:selected").text();
        }

        $("#showinfo").load("/coupons/verify?supplierId=" + supplierId + "&shopId=" + shopId + "&eCouponSn=" + eCouponSn, function (data) {
        });

    });


    $("#sure").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        var shopName = $("#shopName").val();
        if (shopName == undefined) {
            shopName = $("#shopId option:selected").text();
        }
        var shopId = $("#supplierUser_shop_id").val();
        var supplierId = $("#supplierId").val();

        if (eCouponSn == "") {
            $("#checksn").html("<font color=red>请输入券号!</font>");
            $("#statusw").html("");
            return false;
        }
        $.ajax({
            url:"/coupons/update",
            data:"supplierId=" + supplierId + "&shopId=" + shopId + "&eCouponSn=" + eCouponSn + "&shopName=" + shopName,
            type:'POST',
            error:function () {
                alert('消费失败!');
            },
            success:function (data) {
                if (data == '0') {
                    $("#checksn").html("<font color=red>该券消费成功！</font>");
                    $("#statusw").html('券状态:已消费');
                    $("#sure").attr("disabled", false);
                } else if (data == '1') {
                    $("#statusw").html('<font color=red>对不起，该券不能在此门店使用!</font>');
                } else if (data.error=='2') {
                    $("#statusw").html('<font color=red>'+data.info+'</font>');
                } else if (data == 'err') {
                    alert("消费失败！");
                } else {
                    if (data == 'CONSUMED') {
                        data = "消费";
                    } else if (data == 'REFUND') {
                        data = "退款";
                    } else if (data == 'EXPIRED') {
                        data = "过期";
                    } else {
                        data = "处理中";
                    }
                    $("#sure").attr("disabled", false);
                    $("#checksn").html("<font color=red>该券已" + data + "！</font>");
                }

            }
        });
    });
});
