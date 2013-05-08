$(function () {
    //验证姓名
    $("#name").blur(function () {
        checkNameAndCode();
    })
    //验证编码
    $("#code").blur(function () {
        checkNameAndCode();
    })

    $("#save").click(function () {
        var name = $("#name").val();
        if (name == "") {
            $("#checkName").html("<font color=red>请输入姓名!</font>");
            return false;
        }
        var code = $("#code").val();
        if (code == "") {
            $("#checkCode").html("<font color=red>请输入编码!</font>");
            return false;
        }
        var hiddenId = $("#hiddenId").val();
        $.post(
            "/operators/check-name-and-code",
            {id: hiddenId, name: name, code: code},
            function (data) {
                if (data == 'existedName') {
                    $("#checkName").html("<font color=red>对不起，该姓名已经存在!</font");
                } else if (data == 'existedCode') {
                    $("#checkCode").html("<font color=red>对不起，该编码已经存在!</font");
                    $("#checkName").html("");
                } else {
                    $("#checkName").html("");
                    $("#checkCode").html("");
                    $("#operForm").attr("method", "POST");
                    $("#operForm").action = "/operators/create";
                    $("#operForm").submit();
                }
            },
            "text"
        );
    });

});


function checkNameAndCode() {
    var name = $("#name").val();
    var code = $("#code").val();
    var hiddenId = $("#hiddenId").val();
    if (name == "") {
        $("#checkName").html("<font color=red>请输入姓名!</font>");
        return false;
    } else {
        $("#checkName").html("");
    }
    if (code == "") {
        $("#checkCode").html("<font color=red>请输入编码!</font>");
        return false;
    } else {
        $("#checkCode").html("");
    }
    $.post(
        "/operators/check-name-and-code",
        {id: hiddenId, name: name, code: code},
        function (data) {
            if (data == 1) {
                $("#checkName").html("<font color=red>对不起，该姓名已经存在!</font");
            } else if (data == 2) {
                $("#checkCode").html("<font color=red>对不起，该编码已经存在!</font");
                $("#checkName").html("");
            } else {
                $("#checkName").html("");
                $("#checkCode").html("");
            }
        },
        "text"
    );
}
