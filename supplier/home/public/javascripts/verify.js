jQuery(function ($) {

    // 格式化券号
    $('.enter-coupon').live('keypress', function () {
        var _this = $(this),
            value = _this.val();
        if (value.length == 3) {
            _this.val(value + ' ');
        } else if (value.length == 7) {
            _this.val(value + ' ');
        }

        // 仿美团点格式化
        // var val = $(this).val();
        // var arr = val.split('');
        // for (var i = 0, l = arr.length; i < l; i++) {
        // if (i == 3 && arr[i] != " ") {
        // arr.splice(3, 0, " ");
        // l = arr.length;
        // } else if (i == 7 && arr[i] != " ") {
        // arr.splice(7, 0, " ");
        // l = arr.length;
        // } else if (i != 3 && i != 7 && arr[i] == " ") {
        // arr.splice(i, 1);
        // l = arr.length;
        // }
        // }
        // $(this).val(arr.join(''));
    });

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

    checkCouponSn = function (snInput) {
        var shopId = $("input[name='shopId']:checked").val();
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

    $('#check-btn').click(function () {
        if (checkCouponSn(singleCoupon)) {
            singleVerify();
        }
    });

    $('#eCouponSn').keydown("c",function(e) {
        if(e.ctrlKey){//同时按下ctrl+c
            var _this = $(this);
            var value = _this.val();
            if (value.length > 3) {
                _this.val(value + ' ');
            } else if (value.length == 7) {
                _this.val(value + ' ');
            }
        }
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

    var serial = 0, coupons = [];
    enterCoupon = $('#eCouponSn');
    addVerifyQueue = function () {
        if (serial > 10) {
            alert('一次最多只能验证10张券号，请分次验证。');
            return false;
        }

        if (!checkCouponSn(enterCoupon)) {
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
// 直接在input上按下enter键
    enterCoupon.keydown(function (e) {
        if (e.keyCode == 13) {
            addVerifyQueue();
        }
    });
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
});

function getKey(e) {
    e = e || window.event;
    var keycode = e.which ? e.which : e.keyCode;
    if (keycode == 13 || keycode == 108) { //如果按下ENTER键
        //在这里设置你想绑定的事件
        $('#popup-mask').hide();
        $('#popup').hide();
    }
}
// 把keyup事件绑定到document中
function listenKey() {
    if (document.addEventListener) {
        document.addEventListener("keyup", getKey, false);
    } else if (document.attachEvent) {
        document.attachEvent("onkeyup", getKey);
    } else {
        document.onkeyup = getKey;
    }
}
