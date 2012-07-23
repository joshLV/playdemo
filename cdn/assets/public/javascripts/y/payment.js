$(function(){
    var do_not_need_epay = $("#do_not_need_epay").val();
    $("#confirm").click(function(){
        $("#confirm_form").submit();
    });

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
});
