jQuery(function($){
    var totalVoucher = 0, //已选择抵用券数量
        voucherTotalValue = 0, //所选抵用券总金额
        orderTotalMoney = Number($('#j_total-money').text()), //订单总金额
        accountBalance = Number($('#balance').text()), //账户可用余额
        selectedVoucher = $('#j_selected-voucher'),
        paymentBox = $('#payment'),
        otherPayBox = $('#other-pay');

    // 抵用券
    $('#j_voucher').delegate('a, input', 'click', function(){
        var _this = $(this); 
        if (_this.hasClass('use')) {
            $('.voucher-box').show();
        } else if (_this.attr('data-facevalue')) {
            // 当选择的抵用券总金额大于等于订单总金额
            if (voucherTotalValue >= orderTotalMoney) {

                // 不能再选择
                if (_this.attr('checked') == 'checked') {
                    _this.attr('checked', false);
                    selectedVoucher.html('已选择<em>'+ totalVoucher +'</em>张抵用券，抵扣<em>'+ voucherTotalValue +'</em>元，不要再点了哦，再点就浪费了！');
                // 只能取消选择
                } else {
                    totalVoucher--;
                    voucherTotalValue -= Number(_this.attr('data-facevalue'));
                    if (totalVoucher == 0) {
                        selectedVoucher.html('请选择要使用的抵用券');
                    } else {
                        selectedVoucher.html('已选择<em>'+ totalVoucher +'</em>张抵用券，抵扣<em>'+ voucherTotalValue +'</em>元');
                    }
                }

            // 抵用券++
            } else if (_this.attr('checked') == 'checked') {
                totalVoucher++;
                voucherTotalValue += Number(_this.attr('data-facevalue'));
                selectedVoucher.html('已选择<em>'+ totalVoucher +'</em>张抵用券，抵扣<em>'+ voucherTotalValue +'</em>元');

            // 抵用券--
            } else {
                totalVoucher--;
                voucherTotalValue -= Number(_this.attr('data-facevalue'));
                if (totalVoucher == 0) {
                    selectedVoucher.html('请选择要使用的抵用券');
                } else {
                    selectedVoucher.html('已选择<em>'+ totalVoucher +'</em>张抵用券，抵扣<em>'+ voucherTotalValue +'</em>元');
                }
            }
            payCalc();
        }
    });

    // 支付优先级
    // 抵用券 > 余额支付 > 在线支付
    function payCalc() {
        var requireBalancePay = orderTotalMoney - voucherTotalValue,
            requireOnlinePay = requireBalancePay - accountBalance;

        if (requireBalancePay > 0) { //需要用余额支付
            if (requireOnlinePay > 0) { //需要用在线支付
                requireBalancePay = accountBalance;
            } else {
                requireOnlinePay = '0.00';
            }

            paymentBox.slideDown(50);
            otherPayBoxToggle();
        } else {
            requireBalancePay = '0.00';
            requireOnlinePay = '0.00';

            paymentBox.slideUp(50);
            otherPayBox.slideUp(50);
        }

        $('#balance-pay').text(requireBalancePay);
        $('#online-pay').text(requireOnlinePay);
    }
        
    $('#use_balance').click(function(){
        otherPayBoxToggle();
    });

    function otherPayBoxToggle() {
        if ($('#use_balance').attr('checked') == 'checked') {
            if (voucherTotalValue + accountBalance >= orderTotalMoney) {
                otherPayBox.slideUp(50);
            } else {
                otherPayBox.slideDown(50);
            }
        } else {
            otherPayBox.slideDown(50);
        }
    }

    $('#confirm').click(function(){
        var ipt = $('input[name=paymentSourceCode]:checked');

        if (voucherTotalValue < orderTotalMoney) {
            if ($('#use_balance').attr('checked') == 'checked') {
                if (voucherTotalValue + accountBalance < orderTotalMoney) {
                    if (ipt.length == 0) {
                        $('#message').text('请选择支付方式');
                        return false;
                    }
                }
            } else {
                if (ipt.length == 0) {
                    $('#message').text('请选择支付方式');
                    return false;
                }
            }
        }
    });
});
