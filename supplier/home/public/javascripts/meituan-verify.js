/**
 * 用ctrl-v粘贴券号到输入框时自动格式化输入框中的券号，加上空格分开
 * @param e
 * @param $
 */
jQuery(function ($) {
    /**
     * 前端检查券号格式合法性
     *
     * @param shopIdInput
     * @param snInput
     * @return {boolean}
     */
    var checkCouponSn = function (shopIdInput, snInput) {
        var shopId = shopIdInput.val();
        if (shopId == null || shopId == "" || shopId == 0) {
            alert('请选择分店。');
            shopIdInput.focus();
            return false;
        }
        if (snInput.val() == '') {
            alert('请输入券号。');
            snInput.focus();
            return false;
        }

        if (isNaN(snInput.val().replace(/ /g, ''))) {
            alert('券号应为数字，请修正。');
            snInput.focus();
            return false;
        }
        var re = /[0-9]/;   //判断字符串是否为数字
        if (!re.test(snInput.val().replace(/ /g, ''))) {
            alert('券号应为位数字，请修正。');
            snInput.focus();
            return false;
        }
        return true;
    };

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
        // 删除
        if (_this.hasClass('delete-coupon')) {
            var i = $.inArray(_this.parent().next().children('input').val(), coupons);
            if (i >= -1) {
                coupons.splice(i, 1);
                serial--;
                _this.parent().parent().remove();
                // 更新序号
                $('.serial').each(function (i) {
                    $(this).text(i + 1);
                });
            }
            // 批量验证
        } else if (_this.hasClass('batch-verify')) {
            if (_this.hasClass("disabled")) {
                return;
            }
            var eCouponSn = $("#enter-coupon").val().replace(/ /g, '');
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
                            if (data.message.trim() == "消费成功") {
                                data.message = "上海野生动物园135套餐周末票 " + data.message;
                            }
                            $("#verify-info").text(data.message);
                            $('.batch-verify').removeClass("disabled");
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
                            alert(data.errorInfo);
                            enterCoupon.focus();
                            $('.batch-verify').removeClass("disabled");
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
                            $('.batch-verify').removeClass("disabled");
                            $("#verify-btn").text("验证消费");
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
     * 批量验证FORM的点击事件
     */
    $('#coupon-form').delegate('a', 'click', function () {
        var _this = $(this);
        // 批量验证
        if (_this.hasClass('batch-verify')) {
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
                            if (data.message.trim() == "消费成功") {
                                data.message = "上海野生动物园135套餐周末票 " + data.message;
                            }
                            $("#verify-info").text(data.message);
                            $('.batch-verify').removeClass("disabled");
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
//                            alert(data.errorInfo);
                            enterCoupon.focus();
                            $('.batch-verify').removeClass("disabled");
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
                            $('.batch-verify').removeClass("disabled");
                            $("#verify-btn").text("验证消费");
                        }
                    },
                    error: function () {
                        window.location.href = '/verify';
                    }

                });
            }
        }
    });

    /**
     * 输入券号 回车
     */
    $('#coupon-form').keypress(function (e) {
        if (e.keyCode == 13) {
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
                            if (data.message.trim() == "消费成功") {
                                data.message = "上海野生动物园135套餐周末票 " + data.message;
                            }
                            $("#verify-info").text(data.message);
                            $('.batch-verify').removeClass("disabled");
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
//                            alert(data.errorInfo);
                            enterCoupon.focus();
                            $('.batch-verify').removeClass("disabled");
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
                            $('.batch-verify').removeClass("disabled");
                            $("#verify-btn").text("验证消费");
                        }
                    },
                    error: function () {
                        window.location.href = '/verify';
                    }

                });
            }
            return false;
        }


    });
});

