jQuery(document).ready(function(){
    // 加入收藏夹 {{{
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
    // }}}

    // minCarts {{{
    var cart = $('#cart'),
        cartBox = $('#cart .cart-box'),
        cartBoxBd = $('#cart .cart-box-bd');

    cart.mouseover(function(){
        if ($('#reload').val() == 'true') {
            cartBoxBd.load("/carts/tops", function(data) {
                cartBox.addClass('hover');
                $('#reload').val(false);
            });
        } else {
            cartBox.addClass('hover');
        }
    })
    .mouseout(function(){
        cartBox.removeClass('hover');
    });

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
    // }}}

    var cate = $('#cate'),
        item = $('#cate .item'),
        line = $('#cate .line'),
        more = $('#cate .more');
    item.live('mouseover', function(){
        var that = $(this);
        item.removeClass('active');
        that.addClass('active');
        more.hide();
        that.children('.more').show();
        line.hide();
        that.children('.line').show().height(that.height() - 2);
    })
    .live('mouseout', function(){
        $(this).removeClass('active');
        more.hide();
        line.hide();
    });

    // item.delegate('.item', 'mouseover', function(){
        // var that = $(this);
        // item.find('.active').removeClass('active');
        // that.addClass('active');
        // more.hide();
        // that.children('.more').show();
        // line.hide();
        // that.children('.line').show().height(that.height() - 2);
    // })
    // .delegate('.item', 'mouseout', function(){
        // $(this).removeClass('active');
        // more.hide();
        // line.hide();
    // });


    // item.on('mouseover', '.item', function(){
        // var that = $(this);
        // item.find('.active').removeClass('active');
        // that.addClass('active');
        // more.hide();
        // that.children('.more').show();
        // line.hide();
        // that.children('.line').show().height(that.height() - 2);
    // })
    // .on('mouseout', '.item', function(){
        // $(this).removeClass('active');
        // more.hide();
        // line.hide();
    // });

    !$('body.home').length && $('#cate').mouseover(function(){
        $('#cate .menu').show();
    }).mouseout(function(){
        $('#cate .menu').hide();
    });
});
