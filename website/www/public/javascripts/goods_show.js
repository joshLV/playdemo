/**
 * For goods/show.html.
 * User: sujie
 * Date: 2/20/12
 * Time: 11:28 AM
 */
$(window).load(
    function () {
        /**
         *点击加入购物车按钮
         */
        $(link_add_cart).click(function () {
            $.post("/carts", {'goodsId':$("#goodsId").val(), 'number':$("#number").val()},
                function (data) {
                    //                    $('#result').json(data);
                    if (data.resultCode == "ok") {
                        $('#add_cart_result').show();
                    }
                });
            $('#add_cart_result').show();
        });

        $("link_buy_more").click(function () {
            $('#add_cart_result').hide();
        });
    }
);

