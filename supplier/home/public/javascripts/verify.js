/**
 * 用ctrl-v粘贴券号到输入框时自动格式化输入框中的券号，加上空格分开
 * @param e
 * @param $
 */
function formatCopyECouponSn(e, $) {
    if (e.ctrlKey) {//同时按下ctrl+v
        var _this = $(this);
        var value = _this.val();
        if (value.length > 3) {
            _this.val(value.substring(0, 3) + ' ' + value.substring(3, value.length));
            value = _this.val();
        }
        if (value.length > 7) {
            _this.val(value.substring(0, 7) + ' ' + value.substring(7, value.length));
        }
    }
}

jQuery(function ($) {

    //批量验证 格式化券号
    $('.enter-coupon').live('keypress', function () {
        var _this = $(this),
            value = _this.val();
        if (value.length == 3) {
            _this.val(value + ' ');
        } else if (value.length == 7) {
            _this.val(value + ' ');
        }
    });

    //单券验证前查询券的合法性
    var singleCoupon = $('#eCouponSn'),
        singleVerify = function () {
            eCouponSn = singleCoupon.val().replace(' ', '');
            while (eCouponSn.indexOf(" ") >= 0) {
                eCouponSn = eCouponSn.replace(' ', '');
            }

            var shopId = $("input[name='shopId']:checked").val();
            $.get('/verify/single-query',
                {shopId: shopId, eCouponSn: eCouponSn},
                function (data) {
                    // 券号不能通过验证时，给出提示
                    if (data.errorInfo != null && data.errorInfo != "null") {
                        alert(data.errorInfo);
                        return false;

                        // 券号能验证时，让用户确认验证
                    } else {
                        $('#eCouponInfo').html("<p>" + data.supplierName + " " + data.faceValue + "元 <em>(" + data.expireAt + ")</em></p>");
                        $('#popup-mask').show();
                        $('#popup').show();
                    }
                });
        };

    //前端检查券号格式合法性
    checkCouponSn = function (shopIdInput, snInput) {
        var shopId = shopIdInput.val();
        if (shopId == null) {
            alert("请选择门店！");
            return false;
        }

        if (snInput.val() == '') {
            alert('请输入券号');
            snInput.focus();
            return false;
        }
        var re = /[0-9]{3} [0-9]{3} [0-9]{4}/;   //判断字符串是否为数字
        if (!re.test(snInput.val())) {
            alert('券号应为10位数字，请修正');
            snInput.focus();
            return false;
        }
        return true;
    };

    //单券的券验证按钮点击事件，点击后先前端验证合法性，再后台验证，如果合法，则弹出确认验证的小窗口
    $('#check-btn').click(function () {
        if (checkCouponSn($("input[name='shopId']:checked"), singleCoupon)) {
            singleVerify();
        }
    });

    //批量验证的添加券号的按钮点击事件，点击后验证当前输入的券号.
    $('.add-coupon').click(function () {
        if (checkCouponSn($("#shopId"), singleCoupon)) {
            singleVerify();
        }
    });

    $('#eCouponSn').keyup("v", function (e) {
        formatCopyECouponSn.call(this, e, $);
    });

    $('.close').click(function () {
        $('#popup-mask').hide();
        $('#popup').hide();
    });

    $('.cancel').click(function () {
        $('#popup-mask').hide();
        $('#popup').hide();
    });

    $('#verify-btn').click(function () {
        var eCouponSn = $('#eCouponSn').val().replace(' ', '');
        while (eCouponSn.indexOf(" ") >= 0) {
            eCouponSn = eCouponSn.replace(' ', '');
        }
        $('#eCouponSn').val(eCouponSn);
        $('#coupon-form').submit();
    });

    //批量验证的输入框
    enterCoupon = $('#enter-coupon');

    enterCoupon.keyup("v", function (e) {
        formatCopyECouponSn.call(this, e, $);
    });

    // 直接在input上按下enter键
    enterCoupon.keydown(function (e) {
        if (e.keyCode == 13) {
            addVerifyQueue();
        }
    });
    var serial = 0, coupons = [];
    addVerifyQueue = function () {
        if (serial > 10) {
            alert('一次最多只能验证10张券号，请分次验证。');
            return false;
        }

        if (!checkCouponSn($("#shopId"), enterCoupon)) {
            return false;
        }

        var value = enterCoupon.val();
        if ($.inArray(value.replace(/ /g, ''), coupons) != -1) {
            alert('请不要输入重复的券号');
            return false;
        }

        coupons[serial++] = value.replace(/ /g, '');
        $('#coupons-table').append('<tr class="row-coupon' + (serial % 2 == 0 ? " odd" : "") + '">' +
            '<td class="serial">' + serial + '</td>' +
            '<td><a class="delete-coupon" href="javascript:void(0)">删除</a></td>' +
            '<td><input size="12" maxlength="12" type="text" class="enter-coupon" value="' + value + '"></td>' +
            '<td class="verify-result"></td>' +
            '</tr>');
        enterCoupon.val('');
    };


// 代理a上的点击事件
    $('#batch-verify-coupons').delegate('a', 'click', function () {
        var _this = $(this);
        // 删除
        if (_this.hasClass('delete-coupon')) {
            var i = $.inArray(_this.parent().next().children('input').val(), coupons);
            if (i > -1) {
                coupons.splice(i, 1);
                serial--;
                _this.parent().parent().remove();
                // 更新序号
                $('.serial').each(function (i) {
                    $(this).text(i + 1);
                });
            }
            // 清空
        } else if (_this.hasClass('clear-coupon')) {
            if (window.confirm('清空后输入的所有密码将消失，确定吗？')) {
                coupons = [];
                serial = 0;
                $('.row-coupon').remove();
            }
            // 添加
        } else if (_this.hasClass('add-coupon')) {
            addVerifyQueue();
            // 批量验证
        } else if (_this.hasClass('batch-verify')) {
            $.ajax({
                'url': 'template/ajax-batch-verify.php',
                'type': 'POST',
                'dataType': 'JSON',
                'data': 'coupon=' + coupons.join('&coupon='),
                'success': function (data) {
                    var result = data.result;
                    $('.verify-result').each(function (i) {
                        $(this).text(result[i]['errtip']);
                    });
                }
            });
        }
    });


    //
    $('#single-page').click(function () {
        $('#single-page').addClass('curr');
        $('#multi-page').removeClass('curr');
        $('.single-verify').show();
        $('.batch-verify').hide();
        $('#eCouponSn').val('');

    });

    $('#multi-page').click(function () {
        $('#multi-page').addClass('curr');
        $('#single-page').removeClass('curr');
        $('.single-verify').hide();
        $('.batch-verify').show();
        $('#enter-coupon').val('');
    });
});