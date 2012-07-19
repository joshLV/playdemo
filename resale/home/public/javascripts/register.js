$(function(){
	//验证用户名
	$("#loginName").blur(function() {
		checkLoginNameAndMobile();
	})
	//验证手机
	$("#mobile").blur(function() {
		checkLoginNameAndMobile();
	})

	//验证身份证
	$("#identityNo").blur(function() {
		var identityNo = $("#identityNo").val();
		checkIDCard(identityNo);
	})
	
	$("#register_submit").click(function(){
		var loginName = $("#loginName").val();
		if(loginName ==""){
			$("#checkName").html("<font color=red>请输入用户名!</font>");
			return false;
		}
		var mobile = $("#mobile").val();
		if(mobile ==""){
			$("#checkMobile").html("<font color=red>请输入手机!</font>");
			return false;
		}
		
		var identityNo = $("#identityNo").val();
		if (!checkIDCard(identityNo)){
			return false;
		}
		
		$.post(
				"/register/check-resaler",
				{loginName:loginName,mobile:mobile},
				function(data){
					if(data == 1 ){
						$("#checkName").html("<font color=red>对不起，该用户名已经存在!</font");
					} else if(data == 2){
						$("#checkMobile").html("<font color=red>对不起，该手机已经存在!</font");
						$("#checkName").html("");
					} else{
						$("#checkName").html("");
						$("#checkMobile").html("");
						$("#regForm").attr("method","POST");
						$("#regForm").attr("action","/register");
						$("#regForm").submit();
					}
				},
				"text"
		);
	})
	
});


function checkLoginNameAndMobile(){
	var loginName = $("#loginName").val();
	var mobile = $("#mobile").val();
	if(loginName ==""){
		$("#checkName").html("<font color=red>请输入用户名!</font>");
		return false;
	}else {
		$("#checkName").html("");
	}
	if(mobile ==""){
		$("#checkMobile").html("<font color=red>请输入手机!</font>");
		return false;
	} else {
		$("#checkMobile").html("");
	}
	$.post(
			"/register/check-resaler",
			{loginName:loginName,mobile:mobile},
			function(data){
				if(data == 1 ){
					$("#checkName").html("<font color=red>对不起，该用户名已经存在!</font");
				} else if(data == 2){
					$("#checkMobile").html("<font color=red>对不起，该手机已经存在!</font");
					$("#checkName").html("");
				} else {
					$("#checkName").html("");
					$("#checkMobile").html("");
				}
			},
			"text"
	);
}


function checkIDCard (str) { 
	//身份证正则表达式(15位) 
	isIDCard1=/^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$/; 
	//身份证正则表达式(18位) 
	isIDCard2=/^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{4}$/; 
	//验证身份证，返回结果 
	if(!( isIDCard1.test(str)||isIDCard2.test(str))){
		$("#checkIdentityNo").html("<font color=red>请输入有效身份证号码!</font");
		return false;
	} else {
		$("#checkIdentityNo").html("");
	}
	return true;
}
