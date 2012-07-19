/**
 * To SupplierGoods/add.html and edit.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */
$(
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
                $("#form").attr("target","_self");
            } else {
                $("#errorBaseSale").text("上架商品的库存不能为0！");
                return false;
            }
        });
        $("#goods_isAllShop_1").click(function () {
            $("#shop").hide();//隐藏门店列表
        });
        $("#save").click(function () {
            $("#status").val("OFFSALE");
            $("#form").attr("target","_self");
        });
        $("#onsale").click(function () {
            $("#status").val("APPLY");
            $("#form").attr("target","_self");
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
            $("#form").attr("target","_blank");
        });
    }
);
