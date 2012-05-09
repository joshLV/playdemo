/**
 * For goods/show.html.
 * User: sujie
 * Date: 2/20/12
 * Time: 11:28 AM
 */
$(window).load(

    function () {
        /**
         *点击加入分销库按钮
         */
        $("#link_add_cart").click(function () {
            $.post("/library",
                {'goodsIds':$("#goodsId").val()},
                function (data) {
                    if (data.isExist == 1) {
                        $('#add_cart_result1').show();
                        //5秒后自动消失
                        setTimeout("$('#add_cart_result1').css('display','none')", 5000);
                    } else {
                        $('#add_cart_result').show();
                        //5秒后自动消失
                        setTimeout("$('#add_cart_result').css('display','none')", 5000);
                    }
                    var oldSize = parseInt($("#carts-size").html());
                    var buyCount = parseInt($("#number").val());
                    $("#carts-size").html(oldSize + buyCount);
                });
        });

        $("#link_buy_more").click(function () {
            $('#add_cart_result').hide();
        });

        $("#link_buy_now").click(function () {
            $('#buy_now_form').submit();
        });

    }
);

