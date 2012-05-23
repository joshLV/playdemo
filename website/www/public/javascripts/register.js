$(document).ready(
   function() {
    $('a[name=captchaLink]').click(
      function() {
       var randomID = $("#randomID").val();
       $(this).prev().attr(
         'src',
         '/register/captcha?randomID='+randomID+'&t=' + Math.random());
      });
   })
   
//注册提交 
function register(){
	var loginName = $("#loginName").val();
	if(loginName ==""){
		$("#checkName").html("<font color=red>请输入邮箱!</font>");
		return false;
	}
	$.post(
			"/register/checkLoginName",
			{loginName:loginName},
			function(data){
				if(data == 1 ){
					$("#checkName").html("<font color=red>对不起，该邮箱已经存在!</font>");
				} else if(data == 2){
					$("#checkName").html("");
				} else{
					$("#checkName").html("");
					//$("#checkMobile").html("");
					$("#regForm").attr("method","POST");
				    $("#regForm").attr("action","/register");
				    $("#regForm").submit();
				}
			},
			"text"
	);
}

$(window).load(function(){
	//验证用户名
	$("#loginName").blur(function() {
		checkLoginNameAndMobile();
	})

});


function checkLoginNameAndMobile(){
    var loginName = $("#loginName").val();
	var mobile = $("#mobile").val();
	if(loginName ==""){
		$("#checkName").html("<font color=red>请输入邮箱!</font>");
		return false;
	}else {
		$("#checkName").html("");
	}
	$.post(
			"/register/checkLoginName",
			{loginName:loginName},
			function(data){
				if(data == 1 ){
					$("#checkName").html("<font color=red>对不起，该邮箱已已经存在!</font>");
       			} else {
					$("#checkName").html("");
				}
			},
			"text"
	);
}
