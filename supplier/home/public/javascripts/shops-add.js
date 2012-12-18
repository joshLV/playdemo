/**
 * For Shops/add.html.
 * User: sujie
 * Date: 3/16/12
 * Time: 6:07 PM
 */
function districtChange(areaId) {
    $.getJSON("/shops/area?areaId=" + areaId, {}, function (msg) {
        $("#shop_areaId").empty();
        $.each(eval(msg), function (i, item) {
            $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#shop_areaId"));
        });
    });
}

function cityChange(cityId) {
    $("#shop_districtId").empty();
    var url="/shops/area?areaId=" + cityId;
    $.getJSON(url, {}, function (msg) {
        $.each(eval(msg), function (i, item) {
            $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#shop_districtId"));
        });
        districtChange($("#shop_districtId").val());
    });
}

$(function () {
    $("#shop_cityId").change(function () {
        cityChange($("#shop_cityId").val());
    });
    $("#shop_districtId").change(function () {
        districtChange($("#shop_districtId").val());
    });
});
