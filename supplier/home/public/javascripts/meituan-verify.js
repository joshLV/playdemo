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
    var ybqCouponCnt = 0;
    var mtCouponCnt = 0;
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
                ybqCouponCnt++;
            }
            if (eCouponSn.length == 12) {
                mtCouponCnt++;
            }
//            if (ybqCouponCnt > 0 && mtCouponCnt > 0) {
//                $("#verify-msg").text("请确认输入的券号全部是10位或全部是12位！");
//                return false;
//            }

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
                        }
                    },
                    error: function (data) {
                        window.location.href = '/verify';
                    }
                });
            }

//            var i = $.inArray(eCouponSn, couponIds);
//            if (i >= 0) {
//                $("#verify-info-" + index).text("券号输入的有重复，请检查！");
//                return false;
//            }
//            couponIds.push(eCouponSn);
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
                    ybqCouponCnt++;
                }
                if (eCouponSn.length == 12) {
                    mtCouponCnt++;
                }
//                if (ybqCouponCnt > 0 && mtCouponCnt > 0) {
//                    $("#verify-msg").text("请确认输入的券号全部是10位或全部是12位！");
//                    return false;
//                } else {
//                    $("#verify-msg").text("");
//                }
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

//            console.log(eCouponSn+">>>"+couponIds)
//            var i = $.inArray(eCouponSn, couponIds);
//            if (i >= 0) {
//                $("#verify-info-" + index).text("券号输入的有重复，请检查！");
//                success = false;
//                return false;
//            }
//
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
                                    $("#verify-info-" + i).text("上海野生动物园135套餐周末票 " + item.result);
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
                            for (var i=0;i<data.length;i++){
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


    /**
     * FORM的点击事件
     */
    $('#coupon-form').delegate('a', 'click', function () {
        couponIds = [];
        verifyCoupon();
    });

    /**
     * 输入券号 回车
     */
    $('#coupon-form').keypress(function (e) {
        if (e.keyCode == 13) {
            couponIds = []
            verifyCoupon();
            return false;
        }
    });
});

