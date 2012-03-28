/**
 * 商品列表页.
 * User: yjy
 * Date: 3/27/12
 * Time: 11:25 AM
 */

function buyNow(goodsId){
    $('#buy_now_form_'+goodsId).submit();
}

$(window).load(
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
                linkItems[1] = priceFrom;
            } else {
                $("#input_price_from").val("");
                $("#input_price_from").focus();
                return false;
            }
            var priceTo = $("#input_price_to").val();
            if (!isNaN(parseInt(priceTo)) && priceTo > 0) {
                linkItems[2] = priceTo;
            } else {
                $("#input_price_to").val("");
                $("#input_price_to").focus();
                return false;
            }
            $(this).attr('href', linkItems.join("-"));
        });
        
        $("#selectall").click(function () {
            if (this.checked) {
                $("[name='goodsIds[]']").attr("checked", 'true');//全选
            } else {
                $("[name='goodsIds[]']").removeAttr("checked");//取消
            }
        });

        var checkedcnt = 0;
        $("#addto").click(function () {
            $("input[name='goodsIds[]']").each(function () {
                if (this.checked) {
                    checkedcnt++;
                }
            });
            if (checkedcnt == 0) {
                alert("请至少选择一条数据！");
            } else {
                    $("#indexForm").attr("method", "POST");
                    $("#indexForm").attr("action", "/library");
                    $("#indexForm").submit();
            }
        });
        
        /**
         *点击加入分销库按钮
         */
        $("#addToLibrary").click(function () {
            $.post("/library",  {'goodsIds':$("#goodsId").val()});
        });
    }
);




