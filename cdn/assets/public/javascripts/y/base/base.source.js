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

    // 人气指数 {{{
    $('.index-btn').click(function(e){
        e.preventDefault();
        var that = $(this),
            id = that.attr('data-goodsid'),
            tips = that.next('.index-tips');
        $.post('/goods/statistics', {'id': id, 'statisticsType': 'LIKE'}, function(data) {
            $('#summary_' + id).html(data);
            tips.show();
            setTimeout(function() {
                tips.hide();
            }, 5000);
        });
    });
    $('.index-close').click(function(e){
        $(this).parent().hide();
    });
    // }}}

    // 推荐返利 {{{
    $('#J_rebateClose').click(function(e){
        e.preventDefault();
        var _this = $(this),
            _text = $('#J_rebateText');
        if (_text.is(':hidden')) {
            _this.css('background-position', '0 0');
            _text.show(200);
        } else {
            _this.css('background-position', '0 -32px');
            _text.hide(200);
        }
    });

    $.fn.pozFixed = function(params) { 
        var defaults = { 
            top: 320, 
            right: 0
        }; 
        defaults = $.extend(defaults,params);     
        return this.each(function(i,o) { 
            var $doc = $(document),
                $win = $(window),
                $this = $(this); 
            this.fixPosition = function() { 
                var st = $doc.scrollTop();  
                $this.css({
                    position: 'absolute',
                    top: defaults.top + st,
                    right: defaults.left
                });               
            };
            $win.bind('scroll', this.fixPosition);
        });
    };

    if ($.browser.version == '6.0') {
        $('#rebate').pozFixed();
        $('#J_rebateText').mouseenter(function(){
            $('#J_rebateTips').show();
        }).mouseleave(function(){
            $('#J_rebateTips').hide();
        });
    }
    // }}}
});
