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
            $('.onlinepay-bd').append('<span id="onlinepay-error" style="padding:2px;color:#f00">请选择支付方式</span>');;
        } else {
            $("#confirm_form").submit();
        }
    });
});
