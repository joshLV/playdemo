/**
 * To OperateGoods/add.html and edit.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */


$(
    function () {
        $("#goods_supplierId").change(function () {

            $("#brand").load("/goods_brands/" + $("#goods_supplierId").val(), function (data) {
            });
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
//            if ($("#baseSale").val() > 0) {

            $("#status").val("ONSALE");
            $("#form").attr("target", "_self");
//            } else {
//                $("#errorBaseSale").text("上架商品的库存不能为0！");
//                $("#form").attr("target", "_self");
//                return false;
//            }
        });
        $("#goods_isAllShop_1").click(function () {
            $("#shop").hide();//隐藏门店列表
        });
        $("#save").click(function () {
            $("#status").val("OFFSALE");
            $("#form").attr("target", "_self");
        });
        $("#onsale").click(function () {
            $("#status").val("ONSALE");
            $("#form").attr("target", "_self");
        });

        $("#selectWeekDayAll").click(function () {
            if (this.checked) {
                $("[name='useWeekDay[]']").attr("checked", 'true');//全选
            } else {
                $("[name='useWeekDay[]']").removeAttr("checked");//取消
                $("#goods_useWeekDay").val();
            }
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
            $("#form").attr("target", "_blank");
        });
        $("#isLottery").click(function () {
            if (this.checked) {
                this.value = true;
                $("#isLottery").val(true)
                if ($("#goods_supplierId").val() != 5) {
                    $("#err-isLottery").html("抽奖商品只能选择上海视惠信息科技有限公司！").css("color", "#ff0000");
                    $("#goods_supplierId").val(5);
                }
            } else {
                $("#isLottery").val(false)
                $("#err-isLottery").html("");
            }
        });

        $("#isHideOnsale").click(function () {
            if (this.checked) {
                $("#isHideOnsale").val(true)
            } else {
                $("#isHideOnsale").val(false)
            }
        });
        $("#isOrder").click(function () {
            if (this.checked) {
                $("#isOrder").val(true)
            } else {
                $("#isOrder").val(false)
            }
        });
        $("input[name='useWeekDay[]'],#selectWeekDayAll").click(
            function () {
                var week = [];
                if ($(this).attr("checked")) {
                    $("input[name='useWeekDay[]']:checked").each(function () {
                        week.push($(this).val())
                    })
                }
                $("#goods_useWeekDay").val(week.join(","));
            }
        )

        $(".close").click(function (ev) {
            ev.preventDefault();
            var imageId = $(this).attr("imageId");
            $.ajax({
                type:'DELETE',
                url:'/goods_images/' + imageId,
                success:function () {
//                    $("#li_" + imageId).remove();
                    window.location.reload();
                }});
        })

        $(".set-main").click(function (ev) {
            ev.preventDefault();
            var imageId = $(this).attr("imageId");
            var goodsId = $(this).attr("goodsId");
            $.ajax({
                type:'POST',
                url:'/goods_images/' + imageId + "?goodsId=" + goodsId,
                success:function () {
                    window.location.reload();
                }});
        })
    });

