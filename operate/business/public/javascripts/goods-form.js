/**
 * To SupplierGoods/add.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */

//返回val的字节长度
function getByteLen(val) {
    var len = 0;
    for (var i = 0; i < val.length; i++) {
        if (val[i].match(/[^\x00-\xff]/ig) != null) //全角
            len += 2;
        else
            len += 1;
    }
    return len;
}
$(window).load(
    function () {
        $("#goods_supplierId").change(function () {
            $("#tableShop").load("/shops/" + $("#goods_supplierId").val() + "/showGoodsShops", function (data) {
                $("#selectAll").click();
            });
        });

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
        });
        $("#goods_isAllShop_1").click(function () {
            $("#shop").hide();//隐藏门店列表
        });
        $("#onsale").click(function () {
            $("#status").val("ONSALE");
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
            $("#form").attr("target", "_blank");
        });
    }
);
