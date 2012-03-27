$(window).load(function(){
	//验证用户名
	$("#loginName").blur(function() {
		checkLoginNameAndMobile();
	})
    //验证手机
	$("#mobile").blur(function() {
		checkLoginNameAndMobile();
	})
	
	$("#save").click(function() {
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
		var hiddenId = $("#hiddenId").val();
		$.post(
				"/users/checkLoginName",
				{id:hiddenId,loginName:loginName,mobile:mobile},
				function(data){
					if(data == 1 ){
						$("#checkName").html("<font color=red>对不起，该用户名已经存在!</font");
					} else if(data == 2){
						$("#checkMobile").html("<font color=red>对不起，该手机已经存在!</font");
						$("#checkName").html("");
					} else{
						$("#checkName").html("");
						$("#checkMobile").html("");
						$("#operForm").attr("method", "POST");
						$("#operForm").action = "/users/create";
						$("#operForm").submit();
					}
				},
				"text"
		);
	});
	
});


function checkLoginNameAndMobile(){
	var loginName = $("#loginName").val();
	var mobile = $("#mobile").val();
	var hiddenId = $("#hiddenId").val();
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
			"/users/checkLoginName",
			{id:hiddenId,loginName:loginName,mobile:mobile},
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
