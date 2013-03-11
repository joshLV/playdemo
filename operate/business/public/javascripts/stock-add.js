function stockSkuChange(brandId) {
    $.getJSON("/stock_sku/" + brandId, {}, function (msg) {
        var sku = false;

        $("#stock_sku_id").empty();
        $.each(eval(msg), function (i, item) {
            $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#stock_sku_id"));
            $("#err-goods_skuId").html("");
            $("#err-stock_sku_id").html("");
            $("#save").attr('disabled', false);
            sku = true;
            $("#remain_count").empty();
            $.ajax({
                url:'/stock-sku-remain-count/' + $("#stock_sku_id").val(),
                type:'GET',
                datatype:'text',
                error:function () {
                    alert('取得失败!');
                },
                success:function (data) {
                    if (data != null && data != '') {
                        $("#remain_count").html(data + "件");
                    } else {
                        $("#remain_count").html("0件");
                    }
                }
            });
        });
        if (!sku) {
            $("#err-stock_sku_id").html("该品牌没有对应的货品，请添加货品！");
            $("#save").attr('disabled', true);
        }
    });
}

function brandChange(supplierId) {
    $("#stock_brand_id").empty();
    var url = "/stock_brands/" + supplierId;
    $.getJSON(url, {}, function (msg) {
        $.each(eval(msg), function (i, item) {
            $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#stock_brand_id"));
        });
        stockSkuChange($("#stock_brand_id").val());
    });
}

function remainCountChange(skuId) {
    $("#remain_count").empty();
    $.ajax({
        url:'/stock-sku-remain-count/' + skuId,
        type:'GET',
        datatype:'text',
        error:function () {
            alert('取得失败!');
        },
        success:function (data) {
            if (data != null && data != '') {
                $("#remain_count").html(data + "件");
            } else {
                $("#remain_count").html("0件");
            }
        }
    });
}

$(function () {
    $("#supplierName").blur(function () {
        brandChange($("#id_supplierName").val());
    });

    $("#stock_brand_id").change(function () {
        stockSkuChange($("#stock_brand_id").val());
    });

    $("#stock_sku_id").change(function () {
        remainCountChange($("#stock_sku_id").val());
    });

});



$(function () {

    $("#cancel").click(function () {
        $(location).attr('href', '/inventory');
    });
});