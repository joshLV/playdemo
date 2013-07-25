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

    var serial = 0, coupons = [], needClearList = false;

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


    var verifyCoupon = function () {
        var _this = $(this);
        if (_this.hasClass("disabled")) {
            return;
        }
        var eCouponSn = $("#enter-coupon").val().replace(/ /g, '');
        if (eCouponSn.length != 10 && eCouponSn.length != 12) {
            $("#verify-info").text("券号应为10位数字或12位数字，请修正。");
            return false;
        }
        $("#verify-btn").text("正在验证....");
        $("#verify-btn").addClass("disabled");
        if (eCouponSn.length == 12) {
            var partnerGoodsId = $("#partnerGoodsId").val();
            var partnerShopId = $("#partnerShopId").val();
            var goodsId = $("#goodsId").val();

            $('.add-meituan-coupon').addClass("disabled");
            $.ajax({
                type: 'POST',
                data: {'goodsId': goodsId, 'partnerGoodsId': partnerGoodsId, 'partnerShopId': partnerShopId, 'eCouponSn': eCouponSn},
                url: '/meituan-coupon/verified',
                success: function (data) {
                    if (data != null) {
                        if (data.errcode != 1) {
                            data.message = "上海野生动物园135套餐周末票 " + data.message;
                        }
                        $("#verify-info").text(data.message);
                        $("#verify-btn").text("验证消费");
                    }

                }});
        } else {
            $.ajax({
                type: 'POST',
                url: '/verify/' + shopIdInput.val() + "/" + eCouponSn,
                success: function (data) {
                    // 券号不能通过验证时，给出提示
                    if (data.errorInfo != null && data.errorInfo != "null") {
                        enterCoupon.focus();
                        $("#verify-btn").text("验证消费");
                    }
                },
                error: function (data) {
                    window.location.href = '/verify';
                }
            });
            $.ajax({
                type: 'POST',
                url: '/verify/verify',
                data: {'shopId': shopIdInput.val(), 'eCouponSns': eCouponSn},
                success: function (data) {
                    if (data != null) {
                        $("#verify-info").text(data);
                        $("#verify-btn").text("验证消费");
                    }
                },
                error: function () {
                    window.location.href = '/verify';
                }

            });
        }

    };


    /**
     * FORM的点击事件
     */
    $('#coupon-form').delegate('a', 'click', function () {
        verifyCoupon();
    });

    /**
     * 输入券号 回车
     */
    $('#coupon-form').keypress(function (e) {
        if (e.keyCode == 13) {
            verifyCoupon();
            return false;
        }
    });
});

