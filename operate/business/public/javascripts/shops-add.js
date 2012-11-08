/**
 * For OperateShops/add.html.
 * User: sujie
 * Date: 3/16/12
 * Time: 6:07 PM
 */
function loadArea(areaId) {
    var url = "/shops/area";
    $.ajax({
        url:url,
        data:"areaId=" + areaId,
        type:'GET',
        dataType:'JSON',
        error:function () {
            alert('地区读取失败!');
        },
        success:function (msg) {
            $("#shop_areaId").empty();
            $.each(eval(msg), function (i, item) {
                $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#shop_areaId"));
            });
        }
    });
}

function loadDistrict(cityId) {
    var url = "/shops/district";
    $.ajax({
        url:url,
        data:"cityId=" + cityId,
        type:'GET',
        dataType:'JSON',
        error:function () {
            alert('城市读取失败!');
        },
        success:function (msg) {
            $("#shop_districtId").empty();
            $.each(eval(msg), function (i, item) {
                $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#shop_districtId"));
            });
            loadArea($("#shop_districtId").val());
        }
    });
}
$(function () {
    $("#shop_cityId").change(function () {
        loadDistrict($("#shop_cityId").val());
    });

    $("#shop_districtId").change(function () {
        loadArea($("#shop_districtId").val());
    })

});
