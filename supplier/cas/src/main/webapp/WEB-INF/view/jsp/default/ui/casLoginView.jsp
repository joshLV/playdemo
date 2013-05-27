<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>一百券 - 商户中心 - 用户登录</title>
    <link rel="stylesheet" href="css/traders-login.css"/>
    <script src="http://a.uhcdn.com/js/u/jquery-1.7.2.min.js"></script>
    <% String url = request.getServerName();
    System.out.println(url);
        String domain = null;
        if (url.contains("localhost")) {
            domain = "/forget-password";
        } else {
            domain = "http://" + url.substring(0, url.indexOf("."));
            domain += ".admin.quanmx.com/forget-password";
        }
        System.out.println(domain);
    %>

</head>
<%--<jsp:directive.include file="includes/top.jsp" />--%>
<body>
<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true">
<div id="login">
    <h1>一百券<span> - 商户管理中心<span></h1>

    <div class="item">
        <label>用户名：</label>
        <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1"
                    accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true"/>
                  <span id="username-error" class="error"
                        style="display: block; padding-left: 48px; padding-top: 10px;">
                    <form:errors path="*" id="msg"/>
                </span>
    </div>
    <div class="item">
        <label>密　码：</label>
        <form:password cssClass="required" cssErrorClass="error" id="password" size="25" tabindex="2" path="password"
                       accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off"/>

        <span id="password-error" class="error" style="display: block; padding-left: 48px; padding-top: 10px;"></span>
    </div>
    <div class="btn-box">
        <input type="hidden" name="lt" value="${loginTicket}"/>
        <input type="hidden" name="execution" value="${flowExecutionKey}"/>
        <input type="hidden" name="_eventId" value="submit"/>


        <button type="submit" tabindex="3">登 录</button>
        <a class="forgot"
           href="<%=domain%>">忘记密码?</a>
    </div>
    </form:form>
</div>
<div id="footer">©2012 一百券网 quanMX.com 版权所有 沪ICP备08114451号</div>
<script>
    (function ($) {
        var u = $('#username'),
                p = $('#password');
        $('#fm1').submit(function () {
            if (u.val() == '') {
                u.css('border', '1px solid #F50');
                $('#username-error').text('请输入用户名');
                return false;
            }

            if (p.val() == '') {
                p.css('border-color', '1px solid #F50');
                $('#password-error').text('请输入密码');
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
