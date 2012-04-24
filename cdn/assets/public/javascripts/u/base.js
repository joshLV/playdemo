$(function(){
    //  我的优惠啦
    $('.myUhuila').mouseover(function(){
        $(this).addClass('myUhuila-on');
        $('.myUhuila-bd').show();
    });
    $('.myUhuila').mouseout(function(){
        $(this).removeClass('myUhuila-on');
        $('.myUhuila-bd').hide();
    });

    // 购物车
    $('.carts').mouseover(function(){
        $(this).addClass('carts-on');
        $('.carts-bd').show();
    });
    $('.carts').mouseout(function(){
        $(this).removeClass('carts-on');
        $('.carts-bd').hide();
    });
});
