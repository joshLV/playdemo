<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>一百券 - 用户登录</title>
    <%--<link rel="stylesheet" href="http://a.uhcdn.com/css/u/base.css" />--%>
    <%--<link rel="stylesheet" href="http://a.uhcdn.com/css/u/login.css" />--%>
    <script src="http://a.uhcdn.com/js/u/jquery-1.7.2.min.js"></script>
    <%--人人网登录用js--%>
    <script type="text/javascript" src="http://static.connect.renren.com/js/v1.0/FeatureLoader.jsp"></script>

    <link rel="icon" href="http://a.uhcdn.com/images/u/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="http://a.uhcdn.com/images/u/favicon.ico" type="image/x-icon"/>
    <style>
        body, h2, h3, p { padding: 0; margin: 0; }
        body, a, button, input { color: #3E3E3E; font: 12px/1.5 tahoma, arial, \5b8b\4f53, sans-serif; }
        #header { width: 960px; margin: 0 auto; }
        #header img { border: 0; }
        .clearfix:after { content: "."; display: block; height: 0; clear: both; visibility: hidden; }
        .clearfix { *+height: 1%; }

        #content { width: 960px; margin: 0 auto; padding: 25px 0 10px; }
        .form-hd { height: 32px; color: #f97311; line-height: 32px; text-indent: 10px; font-weight: 700; background: url(//img.uhcdn.com/images/y/login.png) repeat-x 0 0; border: 1px solid #e8e8e8; border-top: 3px solid #f97311; }
        .form-bd { padding: 35px 20px; border: solid #D1D1D1; border-width: 0 1px 1px 1px; }
        .login { float: left; width: 420px; padding: 10px 50px 10px 70px; border-right: 1px solid #D1D1D1; }
        .field { padding: 3px 0; }
        .label { display: inline-block; width: 80px; text-align: right; }
        .medium { width: 240px; height: 22px; padding: 0 2px; font-size: 12px; line-height: 22px; border: 1px solid #D1D1D1; }
        .forget { color: #fc7410; padding-left: 5px; text-decoration: none; }
        .error { display: block; width: 204px; height: 20px; padding-left: 52px; color: #f00; }
        #auto { padding-left: 48px; }
        #auto input { position: relative; top: 2px; }
        #remember { position: relative; top: 2px; }
        #submit { width: 127px; height: 37px; margin: 20px 0 0 52px; border: 0; color: #F8F8F8; font-size: 14px; font-weight: 700; text-indent: -9999px; background: url(//img.uhcdn.com/images/y/login.png) no-repeat 0 -33px; cursor: pointer; }

        .guide { float: right; width: 320px; padding-left: 40px; }
        .guide h3 { margin: 0 0; font-size: 14px; line-height: 35px; }
        .guide p { line-height: 20px; }
        .regist { display: block; width: 127px; height: 39px; color: #FFF; margin-top: 20px; font-size: 14px; font-weight: 700; line-height: 39px; text-align: center; text-indent: -9999px; background: url(//img.uhcdn.com/images/y/login.png) no-repeat 0 -71px; }
        #captchaImg { position: relative; top: 8px; }
        #checkcode { width: 70px; }

        #open-auth { margin-top: 25px; }
        #open-auth span { color: #dbdbdb; }
        #open-auth a { color: #005aa0; text-decoration: none; display: inline-block; line-height: 16px; padding-left: 20px; margin: 0 5px; background: url(//img.uhcdn.com/images/y/user/open-auth-btn.png) no-repeat 0 0;}
        #open-auth a.id-qq { background-position: 0 1px; }
        #open-auth a.id-renren { background-position: 0 -31px; }
        #open-auth a.id-weibo { background-position: 0 -65px; }

        #footer { padding: 10px 0 20px; margin: 0 auto; text-align: center; }
        .ft-nav { color: #999; text-align: center; }
        .ft-nav a { color: #3E3E3E; margin: 0 10px; text-decoration: none }
        .ft-nav a:hover { color: #fc7410; text-decoration: none }
        .copyright { color: #999; line-height: 26px; }
        .copyright span { margin: 0 10px; }
    </style>

</head>
<body>
<div id="header">
    <a href="http://www.yibaiquan.com"><img src="http://img.uhcdn.com/images/y/logo.png" alt="一百券"/></a>
</div>
<%
    // 恶心的JSP，用来实现记录当前用户名
    boolean checkRememberUsername = true;
    String lastUsername = "";
    Cookie[] cookies = request.getCookies();
    int n = 0;
    if (cookies != null) {
        n = cookies.length;
        for (int i = 0; i < cookies.length; i++) {
            if ("unremember_user".equals(cookies[i].getName())) {
                checkRememberUsername = false;
            }
            if ("last_username".equals(cookies[i].getName())) {
                lastUsername = cookies[i].getValue();
            }
        }
    }
%>
<div id="content">
    <div class="form-hd">用户登录</div>
    <div class="form-bd clearfix">
        <form:form method="post" class="login" id="login-form" commandName="${commandName}" htmlEscape="true">
            <!--
            <form:errors path="*" id="msg" cssClass="errors" element="div"/>
            -->
            <div class="field">
                <label for="username">用户名：</label>
                <form:input cssErrorClass="error" id="username" class="medium" maxlength="32"
                            size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username"
                            autocomplete="false" htmlEscape="true"/>
                <span id="username-error" class="error">
                    <form:errors path="*" id="msg"/>
                </span>
            </div>
            <div class="field">
                <label for="password">密　码：</label>
                <form:password cssErrorClass="error" id="password" class="medium" maxlength="20" tabindex="2"
                               path="password" accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off"/>
                <a class="forget" id="forget" href="http://www.yibaiquan.com/forget-password">忘记密码？</a>
                <span id="password-error-error" class="error"></span>
            </div>
            <c:if test="${not empty count && count >= 3}">
                <div class="field">
                    <label for="checkcode">验证码：</label>
                    <input type="text" id="checkcode" name="j_captcha_response" maxlength="20" tabindex="3"/>
                    <img id="captchaImg" style="" src="captcha"/>
                    <span>看不清？<a id="newcode" href="" tabindex="4">换一张</a></span>
                    <span id="checkcode-error" class="error"></span>
                </div>
            </c:if>

            <div class="field" id="auto">
                <input type="checkbox" id="rememberUsername" name="rememberUsername" tabindex="5"
                       value="true" <% if (checkRememberUsername) { %> checked="checked" <% } %>/>
                <label for="rememberUsername">记住用户名</label>
            </div>
            <input type="hidden" name="lt" value="${loginTicket}"/>
            <input type="hidden" name="execution" value="${flowExecutionKey}"/>
            <input type="hidden" name="_eventId" value="submit"/>
            <button type="submit" id="submit" tabindex="6"> 登 录</button>
            <div id="open-auth">快捷登录： <a class="id-qq" href="${QQProviderUrl}">QQ</a> <span>|</span>
                <%--<a class="id-renren"--%>
                   <%--href="${RenRenProviderUrl}">人人网</a> <span>|</span>--%>
                <a class="id-weibo" id="sinaweiboAuthorizationUrl"
                   href="${SinaWeiboProviderUrl}">新浪微博</a></div>
        </form:form>
        <div class="guide">
            <h5>还不是一百券用户？</h5>

            <p>惠打折，惠生活；不打折，不消费！立刻免费注册成为一百券用户，轻松享受优惠生活。</p>
            <a class="regist" id="regist" href="http://www.yibaiquan.com/register">注册新用户</a>
        </div>
    </div>
</div>
<script>
    $(function () {

        var username = $('#username'),
                password = $('#password');
        checkcode = $('#checkcode');

        $('#login-form').submit(function () {
            if (username.val() == '') {
                username.css('border', '1px solid #F50');
                $('#username-error').text('请输入用户名');
                return false;
            }
            if (password.val() == '') {
                password.css('border', '1px solid #f50');
                $('#password-error').text('请输入密码');
                return false;
            }
        });
        username.blur(function () {
            username.css('border', '1px solid #D1D1D1');
            $('#username-error').text('');
        });
        password.blur(function () {
            password.css('border', '1px solid #D1D1D1');
            $('#password-error').text('');
        });
        checkcode.blur(function () {
            checkcode.css('border', '1px solid #D1D1D1');
            $('#checkcode-error').text('');
        });
        $('#newcode').click(function (ev) {
            ev.preventDefault();
            var img = $('#captchaImg'),
                    src = img.attr('src').replace(/\?\d*$/, '');
            img.attr('src', src + '?' + +new Date);
        });
    });
</script>

<div id="footer">
    <div class="ft-nav">
        <a href="http://www.yibaiquan.com/about" target="_blank">关于我们</a> |
        <a href="http://www.yibaiquan.com/contact" target="_blank">联系我们</a> |
        <%--<a href="http://quanmx.com/" target="_blank">商户入驻</a> |--%>
        <a href="http://www.yibaiquan.com/help" target="_blank">常见问题</a>
    </div>
    <div class="copyright">
        Copyright © 2012 一百券YiBaiQuan.COM 版权所有 <span> 沪ICP备08114451号</span>
    </div>
</div>
<%--<script type="text/javascript">--%>
    <%--XN_RequireFeatures(["Connect"], function()--%>
    <%--{--%>
        <%--XN.Main.init("cbb8bd3da5bd4950acfbc85caffb891a", "http://www.yibaiquan.com/xd_receiver.html");--%>
    <%--});--%>
<%--</script>--%>
</body>
</html>
