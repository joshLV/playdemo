$(function () {
    //  我的优惠啦
    $('.myUhuila').mouseover(function () {
        $(this).addClass('myUhuila-on');
        $('.myUhuila-bd').show();
    });
    $('.myUhuila').mouseout(function () {
        $(this).removeClass('myUhuila-on');
        $('.myUhuila-bd').hide();
    });

    // 购物车
    $('.carts').mouseover(function () {
        //从服务端加载购物车
        $("#cart-list").load("/carts/tops", function (data) {
        });
        $(this).addClass('carts-on');
        $('.carts-bd').show();
    });
    $('.carts').mouseout(function () {
        $(this).removeClass('carts-on');
        $('.carts-bd').hide();
    });
    //购物车记录删除
    $(".cart-delete").live('click',function (ev) {
        ev.preventDefault();
        var goods_id = $(this).attr("goods_id");
        $.ajax({
            type:'DELETE',
            url:'/carts/' + goods_id,
            success:function (data) {
                $("#row_" + goods_id).remove();
                var oldSize = parseInt($("#carts_size").html());
                var buyCount = parseInt($(this).attr("count"));
                $("#carts_size").html(oldSize - buyCount);
            }});
    });


    $('#slides').slides({
        play:5000,
        pause:2500,
        slideSpeed:600,
        hoverPause:true
    });

    // 详情页tab
    $('#J_tabbar li').click(function () {
        var that = $(this),
            attr = that.attr('name');

        $('#J_tabbar li').removeClass('current');
        that.addClass('current');

        if (attr == 'product-info') {
            $('#J_tabtxt .txt-item').show();
        } else {
            $('#J_tabtxt .txt-item').hide();
            $('#J_tabtxt .' + attr).show();
        }
    });
});
