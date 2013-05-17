<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <!-- Apple devices fullscreen -->
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <!-- Apple devices fullscreen -->
    <meta names="apple-mobile-web-app-status-bar-style" content="black-translucent" />

    <title>运营后台登录</title>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="http://a.uhcdn.com/css/flat/bootstrap.min.css">
    <!-- Bootstrap responsive -->
    <link rel="stylesheet" href="http://a.uhcdn.com/css/flat/bootstrap-responsive.min.css">
    <!-- icheck -->
    <link rel="stylesheet" href="http://a.uhcdn.com/css/flat/plugins/icheck/all.css">
    <!-- Theme CSS -->
    <link rel="stylesheet" href="http://a.uhcdn.com/css/flat/style.css">
    <!-- Color CSS -->
    <link rel="stylesheet" href="http://a.uhcdn.com/css/flat/themes.css">


    <!-- jQuery -->
    <script src="http://a.uhcdn.com/js/flat/jquery.min.js"></script>

    <!-- Nice Scroll -->
    <script src="http://a.uhcdn.com/js/flat/plugins/nicescroll/jquery.nicescroll.min.js"></script>
    <!-- Validation -->
    <script src="http://a.uhcdn.com/js/flat/plugins/validation/jquery.validate.min.js"></script>
    <script src="http://a.uhcdn.com/js/flat/plugins/validation/additional-methods.min.js"></script>
    <!-- icheck -->
    <script src="http://a.uhcdn.com/js/flat/plugins/icheck/jquery.icheck.min.js"></script>
    <!-- Bootstrap -->
    <script src="http://a.uhcdn.com/js/flat/bootstrap.min.js"></script>
    <script src="http://a.uhcdn.com/js/flat/eakroko.js"></script>

    <!--[if lte IE 9]>
    <script src="http://a.uhcdn.com/js/flat/plugins/placeholder/jquery.placeholder.min.js"></script>
    <script>
        $(document).ready(function() {
            $('input, textarea').placeholder();
        });
    </script>
    <![endif]-->


    <!-- Favicon -->
    <link rel="shortcut icon" href="http://a.uhcdn.com/images/flat/favicon.ico" />
    <!-- Apple devices Homescreen icon -->
    <link rel="apple-touch-icon-precomposed" href="http://a.uhcdn.com/images/flat/apple-touch-icon-precomposed.png" />

</head>


<body class='login'>
<div class="wrapper">
    <h1><a href="index.html">
        <img src="http://a.uhcdn.com/images/flat/logo-big.png" alt="" class='retina-ready' width="59" height="49">上海视惠</a></h1>
    <div class="login-body">
        <h2>登录</h2>
        <form:form method="post" id="fm1"  commandName="${commandName}" htmlEscape="true">
            <div class="control-group">
                <div class="email controls">
                    <input type="text" name='username' placeholder="用户名" class='input-block-level' data-
                           rule-required="true" data-rule-email="true">
                </div>
            </div>
            <div class="control-group">
                <div class="pw controls">
                    <input type="password" name="password" placeholder="密码" class='input-block-level' dat
                           a-rule-required="true">
                    <form:errors path="*" id="msg" cssClass="help-block error" element="span" />
                </div>
            </div>
            <div class="submit">

                <input type="hidden" name="lt" value="${loginTicket}" />
                <input type="hidden" name="execution" value="${flowExecutionKey}" />
                <input type="hidden" name="_eventId" value="submit" />
                <input type="submit" value="登录" class='btn btn-primary'>
            </div>
        </form:form>
        <div class="forget">
            <a href="http://www.seewi.com.cn"><span>上海视惠信息科技有限公司</span></a>
        </div>
    </div>
</div>
</body>

</html>
