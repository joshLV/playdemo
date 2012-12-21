$(function(){
    var do_not_need_epay = $("#do_not_need_epay").val();

    $("#use_balance").click(function(){
        if($(this).attr("checked") == "checked"){
            if (do_not_need_epay == "1"){
                $("#other-pay").css("display","none");
                $("#confirm-pay").css("display","block");
            }
        } else {
            if (do_not_need_epay == "1"){
                $("#other-pay").css("display","block");
                $("#confirm-pay").css("display","none");
            }
        }
    });
        
    $('#confirm').click(function(ev){
        ev.preventDefault();
        var ipt = $('input[name=paymentSourceCode]:checked');

        if (ipt.length == 0) {
            if ($('#onlinepay-error').length != 0) {
                $('#onlinepay-error').show();
            } else {
                $('.onlinepay-bd').append('<span id="onlinepay-error" style="padding:2px;color:#f00">请选择支付方式</span>');;
            }
        } else {
            $("#confirm_form").submit();
        }
    });

    var voucherTotalValue = 0, //所选抵用券总金额
        totalVoucher = 0, //已选择抵用券数量
        orderTotalMoney = Number($('#j_total-money').text()), //订单总金额
        accountBalance = Number($('#balance').text()), //账户可用余额
        selectedVoucher = $('#j_selected-voucher');

    $('#j_voucher').delegate('a, input','click', function(){
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
        } else {
            requireBalancePay = '0.00';
            requireOnlinePay = '0.00';
        }

        $('#balance-pay').text(requireBalancePay);
        $('#online-pay').text(requireOnlinePay);

        if (requireOnlinePay == '0.00') {

        }
    }
});
