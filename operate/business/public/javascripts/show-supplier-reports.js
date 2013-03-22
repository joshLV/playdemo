/**
 * 商户资金明细.
 * User: sujie
 * Date: 3/18/13 2:04 PM
 */
$(function () {

    $("[start]").each(function () {
        $(this).click(function () {
            if ($(this).parent().attr("class") == 'active') {
                return false;
            }
            var tomorrow = new Date();
            var startDay = new Date();

            tomorrow.setDate(tomorrow.getDate() + 1);

            var interval = $(this).attr("start");
            var sig = interval.charAt(interval.length - 1);
            var count = parseInt(interval.substring(0, interval.length - 1));
            if (sig == 'd') {
                startDay.setDate(startDay.getDate() - count);
            } else if (sig == 'm') {
                startDay.setMonth(startDay.getMonth() - count);
            } else if (sig == 'y') {
                startDay.setFullYear(startDay.getFullYear() - count);
            }

            $("#condition_begin").val(startDay.getFullYear() + "-" + (startDay.getMonth() + 1) + "-" + startDay.getDate());
            $("#condition_end").val(tomorrow.getFullYear() + "-" + (tomorrow.getMonth() + 1) + "-" + tomorrow.getDate());
            $("#condition_interval").val(interval);
            $("#form").submit();
            return false;
        });
    });

    $("#statistic").click(function () {
        $("#action").val("statistic");
        $("#form").attr("action", "/reports/supplier/statistic");
        $("#form").submit();
    });

    //门店列表跟随商户名联动的方法
    $("#supplierName").blur(function () {
        var found = false;
        var m = list.length;
        var id = null;
        for (i = 0; i < m; i++) {
            if (list[i].name == $("#supplierName").val()) {
                found = true;
                id = list[i].id;
            }
        }
        if (!found) {
            $("#id_supplierName").val(-1);
            $("#save").attr('disabled', true);
            $("#shopId").add(new Option("", "不限"));
        } else {
            console.log("test!!!!!!!!!!!!");
            $("#shop").load("/shops/" + id + "/showIndependentShops", function (data) {
            });
        }
//        $("#supplierUser_shop_id").options[0].selected = true;

        if ($("#supplierName").val().trim() == '') {
            $("#id_supplierName").val(0);
            $("#supplierUser_shop_id").empty();
        }
    });

});
