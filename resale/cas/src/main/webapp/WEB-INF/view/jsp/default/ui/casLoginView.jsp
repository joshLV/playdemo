<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>

<html>
<head>
    <title>分销会员登录</title>
    <meta charset="">
    <link rel="stylesheet" type="text/css" media="screen" href="css/login-cas.css">
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>

</head>
<body>
<div id="header" class="clearfix">
    <h1 id="logo"><img src="images/xlogo.png" alt="券市场"/></h1>
</div>
<div id="login">
    <h2>分销商登录</h2>
    <form:form method="post" id="login-form" commandName="${commandName}" htmlEscape="true">
        <div class="item">
            <label>用户名：</label>
            <form:input cssErrorClass="error" id="username" size="25" tabindex="1"
                        accesskey="${userNameAccessKey}" path="username" autocomplete="false"
                        htmlEscape="true"/>

                  <span id="username-error" class="error">
                    <form:errors path="*" id="msg"/>
                </span>
        </div>
        <div class="item">
            <label>密　码：</label>
            <form:password cssErrorClass="error" id="password" size="25" tabindex="2"
                           path="password" accesskey="${passwordAccessKey}" htmlEscape="true"
                           autocomplete="off"/>

            <span id="password-error-error" class="error"></span>
        </div>
        <div class="btn-box">
            <button type="submit" tabindex="3">登 录</button>
            <a class="forgot" href="http://home.quanfx.com/register">马上注册</a>
        </div>
        <input type="hidden" name="lt" value="${loginTicket}"/>
        <input type="hidden" name="execution" value="${flowExecutionKey}"/>
        <input type="hidden" name="_eventId" value="submit"/>
    </form:form>
</div>
<div id="footer">©2012 券市场 quanFX.com 版权所有 沪ICP备08114451号</div>
<script>
    (function ($) {
        var u = $('#username'),
                p = $('#password');
        $('#login-form').submit(function () {
            if (u.val() == '') {
                u.css('border', '1px solid #F50');
                u.nextAll('.error').text("请输入用户名");
                return false;
            }
            if (p.val() == '') {
                p.css('border-color', '1px solid #F50');
                p.nextAll('.error').text("请输入密码");
                return false;
            }
        });
        u.blur(function () {
            u.css('border-color', '#AAA #DDD #DDD #AAA');
            u.nextAll('.error').text("");
        });
        p.blur(function () {
            p.css('border-color', '#AAA #DDD #DDD #AAA');
            p.nextAll('.error').text("");
        });
    })(jQuery);
</script>

</body>
</html>


