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
        $("#link_add_cart").click(function () {
            $.post("/carts", {'goodsId':$("#goodsId").val(), 'increment':$("#number").val()},
                function (data) {
                    if (data.resultCode == "ok") {
                        $('#add_cart_result').show();
                    }
                });
            $('#add_cart_result').show();
        });

        $("#link_buy_more").click(function () {
            $('#add_cart_result').hide();
        });

        $("#link_buy_now").click(function () {
            $('#buy_now_form').submit();
        });
    }
);

