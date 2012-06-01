$(function () {
    //  我的一百券
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
        if ($("#reload").val() == "true") {
            $("#cart-list").load("/carts/tops", function (data) {
                $("#reload").val("false");
            });
        }
//        $("#carts_size").html($("#goods_count").val());
        $(this).addClass('carts-on');
        $('.carts-bd').show();
    });
    $('.carts').mouseout(function () {
        $(this).removeClass('carts-on');
        $('.carts-bd').hide();
    });
    //购物车记录删除
    $(".cart-delete").live('click', function (ev) {
        ev.preventDefault();
        var buyCount = parseInt($(this).attr("count"));
        var goods_id = $(this).attr("goods_id");
        var sale_price = $(this).attr("sale_price");
        var all_number = parseInt($("#all_number").html());
        var all_price = $("#all_price").html();
        $.ajax({
            type:'DELETE',
            url:'/carts/' + goods_id,
            success:function (data) {
                var oldSize = parseInt($("#carts_size").html());
                var currentCount = oldSize - buyCount;
                $("#carts_size").html(currentCount);

                if (currentCount == 0) {
                    $("#row_" + goods_id).html('您的购物车中没有任何商品。<br/>');
                    $("#order_confirm").hide();
                    $("#detail").hide();
                } else {
                    $("#row_" + goods_id).remove();
                    $("#all_number").html(all_number - buyCount);
                    $("#all_price").html((all_price - (sale_price * buyCount)).toFixed(2));
                }
            }});
    });

    $('#slides').slides({
        play:5000,
        pause:2500,
        slideSpeed:600,
        hoverPause:true
    });
});
