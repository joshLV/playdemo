<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML>
<html>
<head>
        <meta charset="UTF-8">
        <title>一百券 - 用户登录</title>
    <link rel="stylesheet" href="http://a.uhcdn.com/css/u/base.css" />
    <link rel="stylesheet" href="http://a.uhcdn.com/css/u/login.css" />
    <script src="http://a.uhcdn.com/js/u/jquery-1.7.2.min.js"></script>
</head>
<body>
<div id="header" class="clearfix">
    <div id="logo">
        <a href="http://www.yibaiquan.com"><img src="http://a.uhcdn.com/images/u/logo.png" alt="一百券"/></a>
    </div>
</div>
<%
// 恶心的JSP，用来实现记录当前用户名
boolean checkRememberUsername = true;
String lastUsername = "";
Cookie[] cookies = request.getCookies();
int n = 0;
if (cookies!=null) {
  n = cookies.length;
  for (int i=0; i<cookies.length; i++) {
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
    <div class="form-hd"><h2>用户登录</h2><b></b></div>
    <div class="form-bd clearfix">
        <form:form method="post" id="login-form"  commandName="${commandName}" htmlEscape="true">
           <!--
            <form:errors path="*" id="msg" cssClass="errors" element="div" />
             -->
            <div class="field">
                <label for="username">用户名：</label>
                <form:input  cssErrorClass="error" id="username" class="medium" maxlength="32"
                       size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username"
                       autocomplete="false" htmlEscape="true" />
                <span id="username-error" class="error">
                    <form:errors path="*" id="msg" />
                </span>
            </div>
            <div class="field">
                <label for="password">密　码：</label>
                <form:password  cssErrorClass="error" id="password" class="medium" maxlength="20" tabindex="2"
                    path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                <a class="forget" id="forget" href="http://www.yibaiquan.com/forget-password">忘记密码？</a>
                <span id="password-error-error" class="error"></span>
            </div>
            <c:if test="${not empty count && count >= 3}">
            <div class="field">
                <label for="checkcode">验证码：</label>
                <input type="text" id="checkcode" name="j_captcha_response" maxlength="20" tabindex="5" />
                <img id="captchaImg" style="" src="captcha" />
                <span>看不清？<a id="newcode" href="">换一张</a></span>
                <span id="checkcode-error" class="error"></span>
            </div>
            </c:if>

            <div class="field" id="auto">
                <input type="checkbox" id="rememberUsername" name="rememberUsername" tabindex="3" value="true" <% if (checkRememberUsername) { %> checked="checked" <% } %>/>
                <label for="rememberUsername">记住用户名</label>
            </div>
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <button type="submit" id="submit" tabindex="4"> 登 录 </button>
        </form:form>
        <div class="guide">
            <h5>还不是一百券用户？</h5>
            <p>惠打折，惠生活；不打折，不消费！立刻免费注册成为一百券用户，轻松享受优惠生活。</p>
            <a class="regist" id="regist" href="http://www.yibaiquan.com/register">注册新用户</a>
        </div>
    </div>
</div>
<script>
$(function(){

    var username = $('#username'),
        password = $('#password');
        checkcode = $('#checkcode');

    $('#login-form').submit(function(){
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
    username.blur(function(){
        username.css('border', '1px solid #D1D1D1');
        $('#username-error').text('');
    });
    password.blur(function(){
        password.css('border', '1px solid #D1D1D1');
        $('#password-error').text('');
    });
    checkcode.blur(function(){
        checkcode.css('border', '1px solid #D1D1D1');
        $('#checkcode-error').text('');
    });
    $('#newcode').click(function(ev){
        ev.preventDefault();
        var img = $('#captchaImg'),
            src = img.attr('src').replace(/\?\d*$/, '');
        img.attr('src', src +'?'+ +new Date);
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
</body>
</html>

