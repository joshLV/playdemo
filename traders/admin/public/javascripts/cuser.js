$(window).load(function(){
	$("#loginName").blur(function() {
		var loginName = $("#loginName").val();
		if(loginName ==""){
			$("#checkName").html("<font color=red>请输入用户名!</font>");
			return false;
		}

		$.post(
				"/cuser/checkLoginName",
				{loginName:loginName},
				function(data){
					if(data == 1 ){
						$("#checkName").html("<font color=red>对不起，该用户名已经存在!</font");
					} else{
						$("#checkName").html("");
					}
				},
				"text"
		);

	})
	
});

