/**
 * 商品列表页.
 * User: sujie
 * Date: 2/29/12
 * Time: 11:25 AM
 */

function buyNow(goodsId){
    $('#buy_now_form_'+goodsId).submit();
}

$(
    function () {
        /**
         * 排序点击事件
         */
        $(".sort_box").click(function () {
            var oldLink = $(this).attr('href');
            if (oldLink.split("-").length == 8) {//排序点击事件
                if ($(this).hasClass("selected_border")) {
                    if ($(this).hasClass("box_sort_asc")) {
                        $(this).removeClass("box_sort_asc");
                        $(this).addClass("box_sort_desc");
                        $(this).attr('href', oldLink + "-1");
                    } else {
                        $(this).removeClass("box_sort_desc");
                        $(this).addClass("box_sort_asc");
                        $(this).attr('href', oldLink + "-0");
                    }
                } else {
                    $(this).attr('href', oldLink + "-1");
                }
            }
        });

        /**
         * 价格范围的确定点击事件
         */
        $("#link_price_confirm").click(function () {
            var oldLink = $(this).attr('href');
            var linkItems = oldLink.split("-");
            var priceFrom = $("#input_price_from").val();
            if (!isNaN(parseInt(priceFrom)) && priceFrom >= 0) {
                linkItems[5] = priceFrom;
            } else {
                $("#input_price_from").val("");
                $("#input_price_from").focus();
                return false;
            }
            var priceTo = $("#input_price_to").val();
            if (!isNaN(parseInt(priceTo)) && priceTo > 0) {
                linkItems[6] = priceTo;
            } else {
                $("#input_price_to").val("");
                $("#input_price_to").focus();
                return false;
            }
            $(this).attr('href', linkItems.join("-"));
        });
    }
);


