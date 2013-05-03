$(function () {
    //验证姓名
    $("#name").blur(function () {
        checkNameAndMobile();
    })


    $("#save").click(function () {
        var name = $("#name").val();
        if (name == "") {
            $("#checkName").html("<font color=red>请输入姓名!</font>");
            return false;
        }

        var hiddenId = $("#hiddenId").val();
        $.post(
            "/operators/checkName",
            {id: hiddenId, name: name},
            function (data) {
                if (data == 1) {
                    $("#checkName").html("<font color=red>对不起，该姓名已经存在!</font");
                } else {
                    $("#checkName").html("");
                    $("#checkMobile").html("");
                    $("#operForm").attr("method", "POST");
                    $("#operForm").action = "/operators/create";
                    $("#operForm").submit();
                }
            },
            "text"
        );
    });

});


function checkNameAndMobile() {
    var name = $("#name").val();
    var mobile = $("#mobile").val();
    var hiddenId = $("#hiddenId").val();
    if (name == "") {
        $("#checkName").html("<font color=red>请输入姓名!</font>");
        return false;
    } else {
        $("#checkName").html("");
    }

    $.post(
        "/operators/checkName",
        {id: hiddenId, name: name},
        function (data) {
            if (data == 1) {
                $("#checkName").html("<font color=red>对不起，该姓名已经存在!</font");
            } else {
                $("#checkName").html("");
                $("#checkMobile").html("");
            }
        },
        "text"
    );
}
