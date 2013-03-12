function remainCountChange(skuId) {
    $("#remain_count").empty();
    $.ajax({
        url:'/stock-sku-remain-count/' + skuId,
        type:'GET',
        datatype:'text',
        error:function () {
//            alert('请重新操作!');
        },
        success:function (data) {
            if (data != null && data != '') {
                $("#remain_count").html(data + "件");
            } else {
                $("#remain_count").html("0件");
            }
            $("#save").attr('disabled', false);
        }
    });
}

$(function () {
    $("#skuName").blur(function () {
        remainCountChange($("#id_skuName").val());
    });

});


$(function () {
    $("#cancel").click(function () {
        $(location).attr('href', '/inventory');
    });
});

function disableButton() {
    $('#save').attr('disabled', "true");
    $("#save").html("正在提交");
    $('#save').attr('class', "btn disabled");
    return true;
}