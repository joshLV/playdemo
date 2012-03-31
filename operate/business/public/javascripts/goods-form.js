/**
 * To SupplierGoods/add.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */
$(window).load(
    function () {
        $("#goods_topCategoryId").change(function () {
            $("#goods_categories_id").load("/category/sub/" + $("#goods_topCategoryId").val(), function (data) {
                var categoryList = $.parseJSON(data);
                $("#goods_categories_id").empty();
                $.each(categoryList, function (i, category) {
                    $("#goods_categories_id").append("<option value='" + category.id + "'>" + category.name + "</option>");
                });
            });
        });

        $("#onsales").click(function () {
                if ($("#baseSale").val() > 0) {
                    $("#status").val("ONSALE");
                } else {
                    $("#errorBaseSale").text("上架商品的库存不能为0！");
                    return false;
                }
            }
        )
        ;
        $("#goods_isAllShop_2").click(function () {
            $("#shop").show();//显示门店列表
        });
        $("#goods_isAllShop_1").click(function () {
            $("#shop").hide();//隐藏门店列表
        });
        $("#onsale").click(function () {
            $("#status").val("ONSALE");
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
        });
        $("input[name='levelPrices']").onchange(function () {
            alert(this.id);
        });

        $("#level_prices").click(function () {
            
        });
    }
);
