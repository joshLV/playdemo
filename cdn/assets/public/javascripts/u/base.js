$(function () {
    $('#slides').slides({
        play:5000,
        pause:2500,
        slideSpeed:600,
        hoverPause:true
    });

    // 购物车
    var isLoaded = false;
    $('#cart').mouseover(function () {
        if (isLoaded == false) {
            $("#cart-list").load("/carts/tops", function (data) {
                isLoaded = true;
                $('.cart-bd').show();
            });
        } else {
            $('.cart-bd').show();
        }
    });
    $('#cart').mouseout(function () {
        $('.cart-bd').hide();
    });

    //购物车记录删除
    $(".cart-delete").live('click', function (ev) {
        ev.preventDefault();
        var buyCount = parseInt($(this).attr("count")),
            goods_id = $(this).attr("goods_id"),
            sale_price = $(this).attr("sale_price"),
            all_number = parseInt($("#all_number").html()),
            all_price = $("#all_price").html();

        $.ajax({
            type:'DELETE',
            url:'/carts/' + goods_id,
            success:function (data) {
                var oldSize = parseInt($("#cart-count").html());
                var currentCount = oldSize - buyCount;
                $("#cart-count").html(currentCount);

                if (currentCount == 0) {
                    $("#row_" + goods_id).html('您的购物车中没有任何商品。<br/>');
                    $("#order_confirm").hide();
                    $("#detail").hide();
                } else {
                    $("#row_" + goods_id).animate({height: '0'}, 100, function(){
                        $(this).remove();
                    });
                    $("#all_number").html(all_number - buyCount);
                    $("#all_price").html((all_price - (sale_price * buyCount)).toFixed(2));
                }
            }
        });
    });
});
