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
            $("#area").empty();
            $.each(eval(msg), function (i, item) {
                $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#area"));
            });
        }
    });
    $("#area").change();
}

$(document).ready(function () {
    $("#district").change(function () {
        loadArea($("#district").val());
    });


});
