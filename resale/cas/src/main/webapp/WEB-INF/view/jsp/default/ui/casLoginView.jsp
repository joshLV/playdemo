<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>

<html>
    <head>
        <title>分销会员登录</title>
        <meta charset="">
        <link rel="stylesheet" type="text/css" media="screen"   href="public/stylesheets/main.css">
        <link rel="stylesheet" type="text/css" media="screen"   href="public/stylesheets/updateinfo.css">
        <link rel="shortcut icon" type="image/png" href="public/images/favicon.png">
        <script src="public/javascripts/jquery-1.6.4.min.js" type="text/javascript"></script>
    </head>
    <body>
<style type="text/css">
#header {
    height: 123px;
}

.headmain {
    border-bottom: #b73a24 2px solid;
    height: 98px;
    padding-bottom: 10px;
}

.error {
    padding-left: 10px;
    color: red;
}
</style>

<div id="index_main">
    <div id="main">
        <div id="maincontainer" style="overflow: hidden; margin-bottom: 40px; margin-left: 30px;">

            <form:form method="post" id="fm1"  commandName="${commandName}" htmlEscape="true">
                  <form:errors path="*" id="msg" cssClass="errors" element="div" />
            <div style="margin: 145px 18pt 0pt 10px;" class="loginbg">
                <strong style="font-size: 14px; margin-bottom: 6px; display: block;">登录</strong>
                <div class="login">
                    <ul class="loginUl">
                        <li class="field" id="showmess">
                            <div class="pwderror">
                                <h1 class="colorred"></h1>

                            </div>
                        </li>
                        <li class="field">
                            <div class="input">
                                <label>用户名：</label>
                                <form:input  cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
                            </div>
                        </li>

                        <li class="field">
                            <div class="input">
                                <label>密码：</label>
                                <form:password  cssErrorClass="error" id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                            </div>
                        </li>
                        <li class="submit-field">
                        <input type="hidden" name="lt" value="${loginTicket}" />
                        <input type="hidden" name="execution" value="${flowExecutionKey}" />
                        <input type="hidden" name="_eventId" value="submit" />

                        <button id="loginbutton" type="submit"
                                class="bt colorw bold">登录</button>
                </li>
                <li class="fieldnot"><span>还没有分销账号?</span> <span
                    class="colorred">&nbsp;&nbsp;&nbsp;&nbsp;<a href="http://home.114bsgo.com/register">注册</a></span></li>

                </ul>
                <div class="clear"></div>
            </div>
            </form:form>

            <p class="loginfj">
                <span class="colorred">注：</span>如您手机已经绑定过一百券"优卡"(会员卡)，<br>
                &nbsp;&nbsp;&nbsp;&nbsp;并且首次登录网站请<a href="#">点击</a>
            </p>
        </div>
    </div>

</div>

    </body>
</html>


