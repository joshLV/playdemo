$(function () {
    $("#eCouponSn").focus(function () {
        $("#checksn").html("");
        $("#showinfo").html("");
        $("#statusw").html("")
        $("#verify_amount_group").hide();
    });

    $("#query").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#shopId").val();
        if (eCouponSn == "") {
            $("#checksn").html("<font color=red>请输入券号!</font>");
            return false;
        }

        $("#showinfo").load("/coupons/query?shopId=" + shopId + "&eCouponSn=" + eCouponSn, function (data) {
        });

    });


    $("#sure").click(function () {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#shopId").val();
        var verifyAmount = $('#verifyAmount').val();
        if (eCouponSn == "") {
            $("#checksn").html("<font color=red>请输入券号!</font>");
            return false;
        }
        $.ajax({
            url:"/coupons/update",
            data:"shopId=" + shopId + "&eCouponSn=" + eCouponSn + "&verifyAmount=" + verifyAmount,
            type:'POST',
            error:function () {
                alert('消费失败!');
            },
            success:function (data) {
            	var code = data.code;
            	var message = data.info;
                if (code == '0') {
                    $("#checksn").html("<font color=red>该券消费成功！</font>");
                    $("#statusw").html('券状态:已消费');
                    $("#sure").attr("disabled", false);
                } else if (code == '1') {
                    $("#statusw").html('<font color=red>对不起，该券不能在此门店使用!</font>');
                } else if (code == '2') {
                    $("#statusw").html('<font color=red>' + message + '</font>');
                } else if (code == '3') {
                    $("#statusw").html('<font color=red>对不起，该券已冻结！</font>');
                } else if (code == '4') {
                    $("#statusw").html('<font color=red>对不起，该券已过期！</font>');
                } else if (code == '5') {
                    $("#statusw").html('<font color=red>对不起，该券在当当网上为失效状态，可能已过期、已使用或已退款！</font>');
                } else if (code == '6') {
                    $("#statusw").html('<font color=red>请输入合法的验证金额！</font>');
                } else if (code == '7') {
                    $("#statusw").html('<font color=red>' + message + '</font>');
                } else if (code == 'err') {
                    alert("消费失败！");
                } else {
                    if (code == 'CONSUMED') {
                        info = "消费";
                    } else if (code == 'REFUND') {
                        info = "退款";
                    } else if (code == 'EXPIRED') {
                        info = "过期";
                    } else {
                        info = "处理中";
                    }
                    $("#sure").attr("disabled", false);
                    $("#checksn").html("<font color=red>该券已" + info + "！</font>");
                }

            }
        });
    });
});
