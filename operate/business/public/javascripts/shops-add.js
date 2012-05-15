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

$(function () {
    $("#shop_districtId").change(function () {
        loadArea($("#shop_districtId").val());
    });


});
