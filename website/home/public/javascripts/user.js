//用户个人资料验证
function setUserInfo() {
    $("#realnamecheck").html("");
    $("#showsexcheck").html("");
    $("#showbdaycheck").html("");
    $("#showphonecheck").html("");
    $("#showtelcheck").html("");
    $("#showqqcheck").html("");
    var realname = $.trim($("#realname").val());
    var usersex = $(".usersex:checked").val();
    var usertel = $("#usertel").val();
    var userphone = $("#userphone").val();
    var userqq = $("#userqq").val();
    var hobby = [];

    $(".check:checked").each(
        function () {
            hobby.push($(this).val());
        }
    )
    $("#interest").val(hobby.join(","));

    var elselike = $("#elselike").val();
    if (realname == '' || checkRealName(realname)) {
        $("#realnamecheck").html("<img  src='/public/images/error.gif' style='vertical-align:middle'/>&nbsp;&nbsp;&nbsp;请输入正确的姓名");
        return false;
    }
    if (usersex == undefined) {
        $("#showsexcheck").html("<img  src='/public/images/error.gif' style='vertical-align:middle'/>请选择");
        return false;
    }
    if (usertel && checkTel(usertel)) {
        $("#showphonecheck").html("<img  src='/public/images/error.gif' style='vertical-align:middle'/>请输入正确的手机号");
        return false;
    }
    if (userphone && checkPhone(userphone)) {
        $("#showtelcheck").html("<img  src='/public/images/error.gif' style='vertical-align:middle'/>请按提示输入");
        return false;
    }
    if (userqq && checkQQ(userqq)) {
        $("#showqqcheck").html("<img  src='/public/images/error.gif' style='vertical-align:middle'/>请输入正确的QQ号");
        return false;
    }

    $("#userForm").submit();
}

//更换绑定手机
function changeTel(telidentify) {
    if (telidentify == 1) {
        $("#showoldtelcheck").html("");
        $("#shownewtelcheck").html("");
        $("#showchangecode").html("");
        var old_mobile = $.trim($("#old_mobile").val());
        var new_mobile = $.trim($("#new_mobile").val());
        var telcheckcode = $("#telcheckcode").val();
        if (old_mobile == "" && checkTel(old_mobile)) {
            $("#showoldtelcheck").html("原手机号码输入有误!").css("color", "#ff0000");
            $("#showoldtelcheck").css("padding-left", "100px");
            return false;
        }

        if (new_mobile == '' || checkTel(new_mobile)) {
            $("#shownewtelcheck").html("新手机号码输入有误!").css("color", "#ff0000");
            $("#shownewtelcheck").css("padding-left", "100px");
            return false;
        }

        if (telcheckcode == '') {
            $("#showchangecode").html("必须输入验证码!").css("color", "#ff0000");
            $("#showchangecode").css("padding-left", "100px");
            return false;
        }
        $.post(
            "/user-info/mobile-bind",
            {mobile:new_mobile, oldMobile:old_mobile, validCode:telcheckcode},
            function (data) {
                if (data == 0) {
                    $("#layout").hide();
                    //修改成功跳转
                    setTimeout(function () {
                        location.href = "/user-info"
                    }, 10);
                } else if (data == 3) {
                    $("#showoldtelcheck").html("旧手机号码不存在！").css("color", "#ff0000");
                    $("#showoldtelcheck").css("padding-left", "100px");
                } else if (data == 2) {
                    $("#shownewtelcheck").html("输入的手机号码不一致！").css("color", "#ff0000");
                    $("#shownewtelcheck").css("padding-left", "100px");
                } else if (data == 1) {
                    $("#showchangecode").html("验证码有误或已过期").css("color", "#ff0000");
                    $("#showchangecode").css("padding-left", "100px");
                }
            },
            "json"
        );
    } else if (telidentify == 2) {
        $("#jhseccess").hide();
        var bind_mobile = $.trim($("#bind_mobile").val());
        var bindcheckcode = $.trim($("#bindcheckcode").val());
        if (bind_mobile == '') {
            $("#err_bind_mobile").html("请输入手机号！").css("color", "#ff0000");
            $("#err_bind_mobile").css("padding-left", "100px");
            return false;
        }
        if (checkTel(bind_mobile)) {
            $("#err_bind_mobile").html("手机号格式不对！").css("color", "#ff0000");
            $("#err_bind_mobile").css("padding-left", "100px");
            return false;
        }
        if (bindcheckcode == '') {
            $("#showbindtelcheck").html("手验证码不能为空！").css("color", "#ff0000");
            $("#showbindtelcheck").css("padding-left", "100px");
            return false;
        }
        $("#bind_mobile").attr("disabled", "");
        $.post("/user-info/mobile-bind",
            {mobile:bind_mobile, oldMobile:'', validCode:bindcheckcode}, function (data) {
                if (data == 0) {
                    $("#layout").hide();
                    //修改成功跳转
                    setTimeout(function () {
                        location.href = "/user-info"
                    }, 10);
                } else if (data == 2) {
                    $("#err_bind_mobile").html("输入的手机号和不一致！").css("color", "#ff0000");
                    $("#err_bind_mobile").css("padding-left", "100px");
                } else if (data == 1) {
                    $("#showbindtelcheck").html("验证码有误").css("color", "#ff0000");
                    $("#showbindtelcheck").css("padding-left", "100px");
                }
            },
            "text"
        );
    }
}


function getBindCode(codeidy) {
    if (codeidy == 1) {
        var bind_mobile = $.trim($("#bind_mobile").val());
        if (bind_mobile == '') {
            $("#err_bind_mobile").html("请输入手机号！").css("color", "#ff0000");
            $("#err_bind_mobile").css("padding-left", "100px");
            $("#err_bind_mobile").css("display", "block");
            return false;
        }
        if (checkTel(bind_mobile)) {
            $("#err_bind_mobile").html("手机号格式不对！").css("color", "#ff0000");
            $("#err_bind_mobile").css("padding-left", "100px");
            return false;
        }
        $.post(
            "/user-info/send",
            {mobile:bind_mobile, oldMobile:''},
            function (data) {
                if (data == 1) {
                    $("#showbindtelcheck").html("验证码已成功发送，请查收！").css("color", "#ff0000");
                    $("#showbindtelcheck").css("padding-left", "100px");
                    $("#getBindCode").attr("disabled", "");
                } else if (data == 2) {
                    $("#err_bind_mobile").html("该手机已经绑定！").css("color", "#ff0000");
                    $("#err_bind_mobile").css("padding-left", "100px");
                } else if (data == -1) {
                    $("#showbindtelcheck").html("网络错误，请重发").css("color", "#ff0000");
                    $("#showbindtelcheck").css("padding-left", "100px");
                    $("#getBindCode").attr("disabled", "");
                }
            },
            "json"
        );
    } else if (codeidy == 2) {
        var old_mobile = $.trim($("#old_mobile").val());
        if (old_mobile == '') {
            $("#showoldtelcheck").html("请输入原手机号！").css("color", "#ff0000");
            $("#showoldtelcheck").css("padding-left", "100px");
            return false;
        }
        if (checkTel(old_mobile)) {
            $("#showoldtelcheck").html("原手机号格式不对！").css("color", "#ff0000");
            $("#showoldtelcheck").css("padding-left", "100px");
            return false;
        }
        var new_mobile = $.trim($("#new_mobile").val());
        if (new_mobile == '') {
            $("#shownewtelcheck").html("请输入新手机号！").css("color", "#ff0000");
            $("#shownewtelcheck").css("padding-left", "100px");
            return false;
        }
        if (checkTel(new_mobile)) {
            $("#shownewtelcheck").html("新手机号格式不对！").css("color", "#ff0000");
            $("#shownewtelcheck").css("padding-left", "100px");
            return false;
        }

        if (old_mobile == new_mobile) {
            $("#shownewtelcheck").html("新手机号和原手机号一样！").css("color", "#ff0000");
            $("#shownewtelcheck").css("padding-left", "100px");
            return false;
        }
        $.post(
            "/user-info/send",
            {mobile:new_mobile, oldMobile:old_mobile},
            function (data) {
                if (data == 1) {
                    $("#showchangecode").html("验证码已成功发送，请查收！").css("color", "#ff0000");
                    $("#showchangecode").css("padding-left", "100px");
                    $("#getchangeCode").attr("disabled", "");
                } else if (data == 2) {
                    $("#shownewtelcheck").html("该手机已经绑定！").css("color", "#ff0000");
                    $("#shownewtelcheck").css("padding-left", "100px");
                } else if (data == 3) {
                    $("#showoldtelcheck").html("旧手机号码不存在！").css("color", "#ff0000");
                    $("#showoldtelcheck").css("padding-left", "100px");
                } else if (data == -1) {
                    $("#showchangecode").html("网络错误，请重发").css("color", "#ff0000");
                    $("#showchangecode").css("padding-left", "100px");
                    $("#getchangeCode").attr("disabled", "");
                }
            },
            "json"
        );
    }
}

//60秒倒计时
function updateTimer(showspan, timeval) {
    if (timeval >= 0) {
        //timer = el("showspan")
        //timer.innerHTML = TimeToHMS(timeval);
        $("#" + showspan).html(timeval);
        timeval--;
        setTimeout("updateTimer('" + showspan + "', " + timeval + ")", 1000);
    } else {
        timeval = 0;
        $("#" + showspan).html("<input type='button' class='allstep_yzm' onClick='sendCheckCode()' value='重新发送' />");
        //window.location.reload();
    }

}

function checkRealName(realname) {
    var realnamereg = /^([a-zA-Z\u4e00-\u9fa5]* ?)*[a-zA-Z\u4e00-\u9fa5]*$/;
    if (realname.match(realnamereg) == null) {
        return true;
    } else {
        return false;
    }
}
function checkPostalcode(postalcode) {
    var postalcodereg = /^[1-9]\d{5}$/;
    if (postalcode.match(postalcodereg) == null) {
        return true;
    } else {
        return false;
    }
}
function checkPhone(phone) {
    var phonereg = /^0[1-9]{2,3}-[1-9]\d{5,7}$/;
    if (phone.match(phonereg) == null) {
        return true;
    } else {
        return false;
    }
}
//楠岃瘉鎵嬫満鍙风爜
function checkTel(tel) {
    var telreg = /^((15)\d{9})$|^((13)\d{9})$|^((18)\d{9})$/i;
    if (tel.match(telreg) == null) {
        return true;
    } else {
        return false;
    }
}
function checkEmail(email) {

    var reg = /([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)/i;
    if (reg.test(email)) {
        return false;
    } else {
        return true;
    }

}
function checkLoginPass(loginpass) {
    var loginpassreg = /^(?![a-zA-Z]+$)(?![0-9]+$)[a-zA-Z0-9]{6,20}$/;
    if (loginpass.match(loginpassreg) == null) {
        return true;
    } else {
        return false;
    }
}
function checkBDay(bday) {
    var bdayreg = /^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1\d)|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((01,3-9])|(1[0-2]))-(29|30)))))$/;
    if (bday.match(bdayreg) == null) {
        return true;
    } else {
        return false;
    }
}
function checkQQ(qq) {
    var qqreg = /^\s*[.0-9]{5,10}\s*$/;
    if (qq.match(qqreg) == null) {
        return true;
    } else {
        return false;
    }
}
function skippage(skipurl) {
    var pagenum = $("input[@name='pageinput']").val();
    window.location = skipurl + pagenum;
    return false;
}
function checkAllPass(loginpass) {
    var loginpassreg = /^[0-9]{6,20}$/;
    if (loginpass.match(loginpassreg) == null) {
        return true;
    } else {
        return false;
    }
}