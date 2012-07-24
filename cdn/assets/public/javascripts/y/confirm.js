function cal_position_top(id){
    var st=document.documentElement.scrollTop,//滚动条距顶部的距离 
        ch=document.documentElement.clientHeight,//屏幕的高度 
        height=$("#"+id).height();//浮动对象的高度 
    return Number(st)+(Number(ch)-Number(height))/2; 
}
function cal_position_left(id){
    var sl=document.documentElement.scrollLeft,//滚动条距左边的距离 
        cw=document.documentElement.clientWidth,//屏幕的宽度 
        width=$("#"+id).width();//浮动对象的宽度
    return Number(sl)+(Number(cw)-Number(width))/2; 
}
function reset_result_dialog(){
    if($("#full_bg").css("display") == "block"){
        var body_height=document.documentElement.clientHeight,//屏幕的高度
            body_width=document.documentElement.clientWidth;//屏幕的宽度
        $("#full_bg").css({
            width:body_width,
            height:body_height});
        $("#payment_result").css({
            top:cal_position_top("payment_result"),
            left:cal_position_left("payment_result"),
            display:"block"});

    }
}
function close_result_dialog(){
    $("#full_bg").css({display:"none"});
    $("#payment_result").css({display:"none"});
}

$(function() {
    $(window).scroll(function () {
        reset_result_dialog()
    });

    $(window).resize(function () {
        reset_result_dialog()
    });

    $("#confirm_pay").click(function () {
        $("#full_bg").css({display:"block"});
        reset_result_dialog();
        $(this).attr("disabled", "disabled");
        $("#pay_it_form").submit();
    });
});
