<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML>
<html>
<head>
	<meta charset="UTF-8">
	<title>优惠啦 - 用户登录</title>
    <link rel="stylesheet" href="http://a.uhcdn.com/css/u/base.css" />
    <link rel="stylesheet" href="http://a.uhcdn.com/css/u/login.css" />
    <script src="http://a.uhcdn.com/js/u/jquery-1.7.2.min.js"></script>
</head>
<body>
<div id="header" class="clearfix">
    <div id="logo">
        <a href="http://www.uhuila.com/"><img src="http://a.uhcdn.com/images/u/logo.png" width="248" height="88" alt="优惠啦"/></a>
    </div>
</div>

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
                <a class="forget" href="${commandName.substring(commandName.charAt('='),commandName.charAt('/a'))}/forgetPwd">忘记密码？</a>
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
                <input type="checkbox" id="remember" tabindex="3" />
                <label for="remember">记住用户名</label>
            </div>
            <input type="hidden" name="lt" value="${loginTicket}" />
            <input type="hidden" name="execution" value="${flowExecutionKey}" />
            <input type="hidden" name="_eventId" value="submit" />
            <button type="submit" id="submit" tabindex="4"> 登 录 </button>
        </form:form>
        <div class="guide">
            <h5>还不是优惠啦用户？</h5>
            <p>惠打折，惠生活；不打折，不消费！立刻免费注册成为优惠啦用户，轻松享受优惠生活。</p>
            <a class="regist" href="/register">注册新用户</a>
        </div>
    </div>
</div>
<script>
$(function(){
    var username = $('#username'),
        password = $('#password'),
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
        <a href="">关于我们</a> |
        <a href="">联系我们</a> |
        <a href="">人才招聘</a> |
        <a href="">商户入驻</a> |
        <a href="">友情链接</a> |
        <a href="">手机优惠啦</a> |
        <a href="">分销网</a> |
        <a href="">帮助中心</a>
    </div>
    <div class="copyright">
        优惠啦Copyright © 2011-2012 UHUILA.COM 版权所有 <span> 沪ICP备08114451号</span>
    </div>
</div>
</body>
</html>
    