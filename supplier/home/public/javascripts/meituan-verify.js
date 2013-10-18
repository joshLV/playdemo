jQuery(function ($) {
    /**
     * ctrl+v格式券号
     *
     * @param eCouponInput
     * @param e
     */
    var formatCopyECouponSn = function (eCouponInput, e) {
        if (e.ctrlKey) {//同时按下ctrl+v
            var value = eCouponInput.val().replace(/ /g, '');
            if (value.length > 4) {
                eCouponInput.val(value.substring(0, 4) + ' ' + value.substring(4, value.length));
                value = eCouponInput.val();
            }
            if (value.length > 8) {
                eCouponInput.val(value.substring(0, 8) + ' ' + value.substring(9, value.length));
            }
        }
    };

    /**
     * 批量验证的输入框
     */
    var enterCoupon = $('#enter-coupon');
    var shopIdInput = $('#shopId');

    enterCoupon.keyup("v", function (e) {
        formatCopyECouponSn(enterCoupon, e);
    });

    /**
     * 格式化券号输入框中的数字
     */
    enterCoupon.live('keypress', function () {
        var _this = $(this),
            value = _this.val();
        if (value.length == 4) {
            _this.val(value + ' ');
        } else if (value.length == 9) {
            _this.val(value + ' ');
        }
    });
    couponIds = [];
    $(".enter-coupon").each(function () {
        var ele = $(this);
        ele.change(function () {
            var index = ele.attr("coupon-index");
            $('#verify-info-' + index).text("");

            var eCouponSn = ele.val().replace(/ /g, '');
            $('#verify-msg').text("");
            if (eCouponSn == '') {
                $("#verify-info-" + index).text("请输入券号。");
                return;
            }
            if (eCouponSn.length != 10 && eCouponSn.length != 12) {
                $("#verify-info-" + index).text("券号应为10位数字或12位数字，请修正。");
                return;
            }

            if (eCouponSn.length == 10) {
                $.ajax({
                    type: 'POST',
                    url: '/verify/' + shopIdInput.val() + "/" + eCouponSn,
                    success: function (data) {
                        // 券号不能通过验证时，给出提示
                        if (data.errorInfo != null && data.errorInfo != "null") {
                            enterCoupon.focus();
                            $("#verify-info-" + index).text(data.errorInfo);
                            $("#verify-btn").text("验证消费").removeClass("disabled");
                        } else {
                            $("#verify-info-" + index).text("该券商品信息：" + data.goodsName);
                        }
                    },
                    error: function (data) {
                        window.location.href = '/verify';
                    }
                });
            }

        });

    });

    var verifyCoupon = function () {
        var _this = $(this);
        if (_this.hasClass("disabled")) {
            return false;
        }
        $('#verify-msg').text("");

        var success = true;
        $(".enter-coupon").each(function () {
            var ele = $(this);
            var index = ele.attr("coupon-index");
            var eCouponSn = ele.val().replace(/ /g, '');

            $(ele).change(function () {
                $('#verify-info-' + index).text("");
                if (eCouponSn.length != 10 && eCouponSn.length != 12) {
                    $("#verify-info-" + index).text("券号应为10位数字或12位数字，请修正。");
                    success = false;
                    return;
                }

                if (eCouponSn.length == 10) {
                    $.ajax({
                        type: 'POST',
                        url: '/verify/' + shopIdInput.val() + "/" + eCouponSn,
                        success: function (data) {
                            // 券号不能通过验证时，给出提示
                            if (data.errorInfo != null && data.errorInfo != "null") {
                                enterCoupon.focus();
                                $("#verify-info-" + index).text(data.errorInfo);
                                $("#verify-btn").text("验证消费").removeClass("disabled");
                                success = false;
                            }
                        },
                        error: function (data) {
                            window.location.href = '/verify';
                        }
                    });
                }
            });

            if (eCouponSn != '') {
                couponIds.push(eCouponSn);
            }

        });


        if (couponIds.length == 0) {
            $("#verify-msg").text("请输入券号！");
            return false;
        }
        if (success) {
            $("#verify-btn").text("正在验证....").addClass("disabled");
            if (couponIds[0].length == 12) {
                var partnerGoodsId = $("#partnerGoodsId").val();
                var partnerShopId = $("#partnerShopId").val();
                var goodsId = $("#goodsId").val();

                $.ajax({
                    type: 'POST',
                    data: {'goodsId': goodsId, 'partnerGoodsId': partnerGoodsId, 'partnerShopId': partnerShopId, 'couponIds': couponIds},
                    url: '/meituan-coupon/verified',
                    success: function (data) {
                        if (data != null) {
                            var msg = [];
                            $.each(eval(data), function (i, item) {
                                if (item.errcode != 1) {
                                    $("#verify-info-" + i).text(item.goodsname + " " + item.result);
                                } else {
                                    $("#verify-info-" + i).text(item.result);
                                }
                                $("#verify-btn").text("验证消费").removeClass("disabled");
                            })

                        }

                    }});
            } else {
                $.ajax({
                    type: 'POST',
                    url: '/verify/verify',
                    data: {'shopId': shopIdInput.val(), 'eCouponSns': couponIds},
                    success: function (data) {
                        if (data != null) {
                            for (var i = 0; i < data.length; i++) {
                                $("#verify-info-" + i).text(data[i]);
                                $("#verify-btn").text("验证消费").removeClass("disabled");
                            }
                        }
                    },
                    error: function () {
                        window.location.href = '/verify';
                    }

                });
            }
        }
    };


//    /**
//     * FORM的点击事件
//     */
//    $('#coupon-form').delegate('a', 'click', function () {
//        couponIds = [];
//        verifyCoupon();
//    });

//    /**
//     * 输入券号 回车
//     */
//    $('#coupon-form').keypress(function (e) {
//        if (e.keyCode == 13) {
//            couponIds = []
//            verifyCoupon();
//            return false;
//        }
//    });

    $("#verify-btn").click(function () {
        couponIds = [];
        verifyCoupon();
    })

    $("#dp-verify-btn").click(function () {
        var _this = $(this);
        if (_this.hasClass("disabled")) {
            return false;
        }
        $('#dp-verify-msg').text("");

        var noError = false;
        $(".dp-enter-coupon").each(function () {
            var ele = $(this);
            var index = ele.attr("dp-coupon-index");
            var eCouponSn = ele.val().replace(/ /g, '');
            $(ele).change(function () {
                $('#dp-verify-info-' + index).text("");
                if (eCouponSn.length != 10) {
                    $("#dp-verify-info-" + index).text("券号应为10位数字，请修正。");
                    noError = true;
                }
            });
            if (eCouponSn != '') {
                couponIds.push(eCouponSn);
            }
            return true;
        });

        if (!noError && couponIds.length == 0) {
            $("#dp-verify-msg").text("请输入券号！");
            return false;
        }
        if (!noError) {
            $("#dp-verify-btn").text("正在验证....").addClass("disabled");

            $.ajax({
                type: 'POST',
                data: {'couponIds': couponIds},
                url: '/dianping-coupon/verified',
                success: function (data) {
                    if (data != null) {
                        for (var i = 0; i < data.length; i++) {
                            $("#dp-verify-info-" + i).text(data[i].result.msg.message);
                            $("#dp-verify-btn").text("验证消费").removeClass("disabled");
                        }
                    }
                },
                error: function () {
                    $("#dp-verify-msg").text("验证失败！");
                    $("#dp-verify-btn").text("验证消费").removeClass("disabled");
                }

            });
        }

    })

    $("#nm-verify-btn").click(function () {
        var _this = $(this);
        if (_this.hasClass("disabled")) {
            return false;
        }
        $('#nm-verify-msg').text("");

        var ele = $(".nm-enter-coupon")
        var eCouponSn = ele.val().replace(/ /g, '');
        if (eCouponSn == '') {
            $("#nm-verify-msg").text("请输入券号！");
            return false;
        }
        $('#nm-verify-info').text("");
        if (eCouponSn.length != 10) {
            $("#nm-verify-info").text("券号应为10位数字，请修正。");
            return false;
        }
        $("#nm-verify-btn").text("正在验证....").addClass("disabled");

        $.ajax({
            type: 'POST',
            data: {'couponId': eCouponSn},
            url: '/nuomi-coupon/verified',
            success: function (data) {
                console.log(data)
                if (data != null && data.isSucess) {
                    $("#nm-verify-info").text(data.wrongCheckMsg);
                    $("#nm-verify-btn").text("验证消费").removeClass("disabled");
                }
            },
            error: function () {
                $("#nm-verify-msg").text("验证失败！");
                $("#nm-verify-btn").text("验证消费").removeClass("disabled");
            }

        });

    })
})
;

