$(function($){
    var addToFavorite = function(url, title) {
        try {
            window.external.AddFavorite(url, title);
        } catch(e) {
            try {
                window.sidebar.addPanel(title, url, '');
            } catch(e) {
                alert('对不起，您的浏览器不支持自动收藏，请使用Ctrl+D进行手动收藏！');
            }
        }
    };
    $('#favorite').click(function(evt) {
        evt.preventDefault();
        var url = window.location,
            title = document.title;
        addToFavorite(url, title);
    });

    var cart = $('#cart'),
        cartBox = $('#cart .cart-box'),
        cartBoxBd = $('#cart .cart-box-bd');

    cart.mouseover(function(){
        cartBox.addClass('hover').addClass('loading');
        cartBoxBd.load("/carts/minCarts", function(data) {
            isLoaded = true;
            cartBoxBd.removeClass('loading');
        });
    })
    .mouseout(function(){
        cartBox.removeClass('hover');
    });

    //购物车记录删除
    $('.goods-del').live('click', function (ev) {
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
                    cartBoxBd.html('<div class="emptycart">你的购物车里没有任何商品。</div><div class="totalcart">共<em>0</em>件商品，共计<em>0.00</em>元</div>');
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
