function checkResaler(id, status, flg) {
    var remark = $("#remark").val();
    if (flg == 0 && (remark == "" || remark == null)) {
        $("#checkRemark").html("请输入备注！");
    } else {
        $("#checkRemark").html("");

    }
    var url = "/resalers/" + id + "/status?status=" + status + "&remark=" + remark;
    $("#checkFrm").attr("method", "POST");
    $("#checkFrm").attr("action", url);
    $("#checkFrm").submit();

}

$(document).ready(function () {
    $("#edit").click(function () {
        $("#flag").val("0");
        $("#flagForm").submit();
    });
    $("#view").click(function () {
        $("#flag").val("1");
        $("#flagForm").submit();
    });

}); 

