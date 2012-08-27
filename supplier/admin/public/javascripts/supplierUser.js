$(function () {
    //验证用户名
    $("#loginName").blur(function () {
        checkLoginNameAndMobile();
    })
    //验证工号
    $("#jobNumber").blur(function () {
        checkLoginNameAndMobile();
    })
    //验证手机
    $("#mobile").blur(function () {
        checkLoginNameAndMobile();
    })

    $("#save").click(function () {
        var loginName = $("#loginName").val();
        if (loginName == "") {
            $("#checkName").html("<font color=red>请输入用户名!</font>");
            return false;
        }
        var mobile = $("#mobile").val();
        if (mobile == "") {
            $("#checkMobile").html("<font color=red>请输入手机!</font>");
            return false;
        }
        var jobNumber = $("#jobNumber").val();
        if (jobNumber == "") {
            $("#checkJobNumber").html("<font color=red>请输入工号!</font>");
            return false;
        } else {
            if (!Number(jobNumber)) {
                $("#checkJobNumber").html("<font color=red>工号只能为数字!</font>");
                return false;
            }
            $("#jobNumber").html("");
        }

        var hiddenId = $("#hiddenId").val();
        $.post(
            "/users/checkLoginName",
            {id:hiddenId, loginName:loginName, mobile:mobile,jobNumber:jobNumber},
            function (data) {
                if (data == 1) {
                    $("#checkName").html("<font color=red>对不起，该用户名已经存在!</font>");
                } else if (data == 2) {
                    $("#checkMobile").html("<font color=red>对不起，该手机已经存在!</font>");
                    $("#checkName").html("");
                } else if (data == 3) {
                    $("#checkJobNumber").html("<font color=red>对不起，该手机工号存在!</font>");
                    $("#checkName").html("");
                } else {
                    $("#checkName").html("");
                    $("#checkMobile").html("");
                    $("#operForm").attr("method", "POST");
                    $("#operForm").action = "/users/" + hiddenId;
                    $("#operForm").submit();
                }
            },
            "text"
        );
    });

});


function checkLoginNameAndMobile() {
    var loginName = $("#loginName").val();
    var mobile = $("#mobile").val();
    var jobNumber = $("#jobNumber").val();
    var hiddenId = $("#hiddenId").val();
    if (loginName == "") {
        $("#checkName").html("<font color=red>请输入用户名!</font>");
        return false;
    } else {
        $("#checkName").html("");
    }
    if (mobile == "") {
        $("#checkMobile").html("<font color=red>请输入手机!</font>");
        return false;
    } else {
        $("#checkMobile").html("");
    }
    if (jobNumber == "") {
        $("#checkJobNumber").html("<font color=red>请输入工号!</font>");
        return false;
    } else {
        if (!Number(jobNumber)) {
            $("#checkJobNumber").html("<font color=red>工号只能为数字!</font>");
            return false;
        }
        $("#jobNumber").html("");
    }

    $.post(
        "/users/checkLoginName",
        {id:hiddenId, loginName:loginName, mobile:mobile,jobNumber:jobNumber},
        function (data) {
            if (data == 1) {
                $("#checkName").html("<font color=red>对不起，该用户名已经存在!</font>");
            } else if (data == 2) {
                $("#checkMobile").html("<font color=red>对不起，该手机已经存在!</font>");
                $("#checkName").html("");
            } else if (data == 3) {
                $("#checkJobNumber").html("<font color=red>对不起，该手机工号存在!</font>");
                $("#checkName").html("");
            } else {
                $("#checkName").html("");
                $("#checkMobile").html("");
            }
        },
        "text"
    );
}
