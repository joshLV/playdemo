/**
 * 用ctrl-v粘贴券号到输入框时自动格式化输入框中的券号，加上空格分开
 * @param e
 * @param $
 */
jQuery(function ($) {
    checkShop = function (shopIdInput) {
        var shopId = shopIdInput.val();
        if (shopId == null || shopId.trim() == "" || shopId == 0) {
            if (shopId.trim() != '') {
                alert('您输入的分店不存在，请选择分店。');
                shopIdInput.focus();
            } else {
                alert('请选择分店。');
                shopIdInput.focus();
            }

            return false;
        }
        return true;
    };

    /**
     * 前端检查券号格式合法性
     *
     * @param shopIdInput
     * @param snInput
     * @return {boolean}
     */
    checkCouponSn = function (shopIdInput, snInput) {
        if (!checkShop(shopIdInput)) {
            return false;
        }

        if (snInput.val() == '') {
            alert('请输入券号。');
            snInput.focus();
            return false;
        }
        var re = /[0-9]{3} [0-9]{3} [0-9]{4}/;   //判断字符串是否为数字
        if (!re.test(snInput.val())) {
            alert('券号应为10位数字，请修正。');
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
    formatCopyECouponSn = function (eCouponInput, e) {
        if (e.ctrlKey) {//同时按下ctrl+v
            var value = eCouponInput.val().replace(/ /g, '');
            if (value.length > 3) {
                eCouponInput.val(value.substring(0, 3) + ' ' + value.substring(3, value.length));
                value = eCouponInput.val();
            }
            if (value.length > 7) {
                eCouponInput.val(value.substring(0, 7) + ' ' + value.substring(7, value.length));
            }
        }
    }

    var serial = 0, coupons = [], needClearList = false;

    /**
     * 批量验证的输入框
     */
    enterCoupon = $('#enter-coupon');
    shopIdInput = $('#id_shopName');

    enterCoupon.keyup("v", function (e) {
        formatCopyECouponSn(enterCoupon, e);
    });

    /**
     * 格式化券号输入框中的数字
     */
    enterCoupon.live('keypress', function () {
        var _this = $(this),
            value = _this.val();
        if (value.length == 3) {
            _this.val(value + ' ');
        } else if (value.length == 7) {
            _this.val(value + ' ');
        }
    });

    addMultiVerifyCoupon = function () {
        if (serial > 10) {
            alert('一次最多只能验证10张券号，请分次验证。');
            return false;
        }

        var value = enterCoupon.val();
        if ($.inArray(value.replace(/ /g, ''), coupons) != -1) {
            alert('请不要输入重复的券号。');
            return false;
        }

        if (checkCouponSn(shopIdInput, enterCoupon)) {
            addVerifyQueue();
            if (needClearList) {
                clearList();
                needClearList = false;
            }
        }
    };

    /**
     * 点击门店输入框显示下拉列表.
     */
    $('#shopName').click(function () {
        $(".ac_input").keydown();
        $(".acResults").show();
    });

    /**
     * 增加待验证的券到列表中.
     */
    addVerifyQueue = function () {
        var value = enterCoupon.val();
        var eCouponSn = value.replace(/ /g, '');

        $.ajax({
            type: 'POST',
            url: '/verify/' + shopIdInput.val() + "/" + eCouponSn,
            success: function (data) {
                // 券号不能通过验证时，给出提示
                if (data.errorInfo != null && data.errorInfo != "null") {
                    alert(data.errorInfo);
                    enterCoupon.focus();
                    return;
                }
                coupons[serial++] = eCouponSn;
                $("#eCouponSns").val(coupons.join(','));
                // 券号能验证时，让用户确认验证
                $('#coupons-table').append('<tr class="row-coupon' + (serial % 2 == 0 ? " odd" : "") + '">' +
                    '<td class="serial">' + serial + '</td>' +
                    '<td>' + value + '</td>' +
                    '<td>' + data.goodsName + '</td>' +
                    '<td>' + data.faceValue + '元</td>' +
                    '<td>' + data.expireAt + '</td>' +
                    '<td><a class="delete-coupon" href="javascript:void(0)">删除</a></td>' +
                    '<td class="verify-result"></td>' +
                    '</tr>');
                enterCoupon.val('');
                $('.batch-verify').removeClass("disabled");
            },
            error: function (data) {
                window.location.href = '/';
            }
        });
//        $.post('/verify/' + shopIdInput.val() + "/" + eCouponSn,
//            function (data) {
//                // 券号不能通过验证时，给出提示
//                if (data.errorInfo != null && data.errorInfo != "null") {
//                    alert(data.errorInfo);
//                    enterCoupon.focus();
//                    return;
//                }
//                coupons[serial++] = eCouponSn;
//                $("#eCouponSns").val(coupons.join(','));
//                // 券号能验证时，让用户确认验证
//                $('#coupons-table').append('<tr class="row-coupon' + (serial % 2 == 0 ? " odd" : "") + '">' +
//                    '<td class="serial">' + serial + '</td>' +
//                    '<td>' + value + '</td>' +
//                    '<td>' + data.goodsName + '</td>' +
//                    '<td>' + data.faceValue + '元</td>' +
//                    '<td>' + data.expireAt + '</td>' +
//                    '<td><a class="delete-coupon" href="javascript:void(0)">删除</a></td>' +
//                    '<td class="verify-result"></td>' +
//                    '</tr>');
//                enterCoupon.val('');
//                $('.batch-verify').removeClass("disabled");
//            });
//
    };

    clearList = function () {
        coupons = [];
        $("#eCouponSns").val('');
        serial = 0;
        $('.row-coupon').remove();
        $('.batch-verify').addClass("disabled");

    };

    $('#coupon-form').keypress(function (e) {
        if (e.keyCode == 13) {
            addMultiVerifyCoupon();
            return false;
        }
    });


    /**
     * 批量验证FORM的点击事件
     */
    $('#coupon-form').delegate('a', 'click', function () {
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
            //批量验证的添加券号的按钮点击事件，点击后验证当前输入的券号.
        } else if (_this.hasClass('add-coupon')) {
            addMultiVerifyCoupon();
            // 批量验证
        } else if (_this.hasClass('batch-verify')) {
            if (_this.hasClass("disabled")) {
                return;
            }
            if (!checkShop()) {
                return;
            }
            $.ajax({
                type: 'POST',
                url: '/verify/verify',
                data: {'shopId': shopIdInput.val(), 'eCouponSns': coupons},
                success: function (data) {
                    if (data != null) {
                        $('.verify-result').each(function (i) {
                            $(this).text(data[i]);

                            if (data[i] != null && data[i].indexOf("成功") >= 0) {
                                $(this).addClass("success")
                            } else {
                                $(this).addClass("error")
                            }

                        });
                        needClearList = true;
                        $('.batch-verify').addClass("disabled");
                    }
                }

            });
        }
    });
});