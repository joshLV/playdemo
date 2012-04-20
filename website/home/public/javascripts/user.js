//用户个人资料验证
function setUserInfo(){
	$("#realnamecheck").html("");
	$("#showsexcheck").html("");
	$("#showbdaycheck").html("");
	$("#showphonecheck").html("");
	$("#showtelcheck").html("");
	$("#showqqcheck").html("");
	var realname       = $.trim($("#realname").val());
	var usersex        = $(".usersex:checked").val();
	var bdayyear       = $("#bdayyear").val();
	var bdaymonth      = $("#bdaymonth").val();
	var bdayday        = $("#bdayday").val();
	var usertel        = $("#usertel").val();
	var userphone      = $("#userphone").val();
	var userqq         = $("#userqq").val();
	var marrstate      = $(".marrstate:checked").val();
	var industry       = $("#industry").val();
	var position       = $("#position").val();
	var salary         = $("#salary").val();
	var bday           = $.trim(bdayyear)+'-'+$.trim(bdaymonth)+'-'+$.trim(bdayday);
	var hobby = [];
	var likenames = [];
	$(".likenames:checked").each(
			function(){
				hobby.push($(this).val());
			}
	)
	$("#intrest").val(hobby.join(","));

	var elselike      = $("#elselike").val();
	if(realname==''||checkRealName(realname)){
		$("#realnamecheck").html("<div class='loginpwd_msg'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>&nbsp;&nbsp;&nbsp;请输入正确的姓名</div></div>");
		return false;
	}
	if(usersex==undefined){
		$("#showsexcheck").html("<div class='loginpwd_msg' style='margin-right:10px'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>请选择</div></div>");
		return false;
	}
	if(bdayyear==''||bdaymonth==''||bdayday==''||checkBDay(bday)){
		$("#showbdaycheck").html("<div class='loginpwd_msg' style='margin-left:10px'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>请输入正确的生日</div></div>");  
		return false;
	}

	if(usertel&&checkTel(usertel)){
		$("#showphonecheck").html("<div class='loginpwd_msg' style='margin-left:5px'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>请输入正确的手机号</div></div>");
		return false;
	}
	if(userphone&&checkPhone(userphone)){
		$("#showtelcheck").html("<div class='loginpwd_msg'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>请按提示输入</div></div>");
		return false;
	}
	if(userqq&&checkQQ(userqq)){
		$("#showqqcheck").html("<div class='loginpwd_msg'><div class='attention'><img  src='/public/images/error.gif' style='vertical-align:middle'/>请输入正确的QQ号</div></div>");
		return false;
	}

	$("#userForm").submit();
}

//更换绑定手机
function changeTel(telidentify){
	if(telidentify==1){
		$("#showoldtelcheck").html("");
		$("#shownewtelcheck").html("");
		$("#showchangecode").html("");
		var oldtelnum    = $.trim($("#oldtelnum").val());
		var newtelnum    = $.trim($("#newtelnum").val());
		var telcheckcode = $("#telcheckcode").val();
		if(oldtelnum&&checkTel(oldtelnum)){
			alert("原手机号码输入有误!");
			return false;
		}
		if(newtelnum==''||checkTel(newtelnum)){
			alert("新手机号码输入有误!");
			return false;
		}
		if(telcheckcode==''){
			alert("必须输入验证码!");
			return false;
		}
		$("#newtelnum").attr("disabled","");
		$.post(
				"/userInfo/bindMobile",
				{mobile:newtelnum,validCode:telcheckcode,telidentify:1},
				function(data){
					alert(data)
					if(data==0){
						$("#layout").hide();
						//修改成功跳转
						setTimeout(function(){ location.href = "/userInfo"},10);
					}else if(data==-1){
						alert('时间过期');
					}else if(data==2){
						alert('输入的手机号和不一致！');
					}else if(data==1){
						alert('验证码有误');
					}
				}, 
				"json"
		);
	}else if(telidentify==2){
		$("#jhseccess").hide();
		var bindtelnum      = $.trim($("#bindtelnum").val());
		var bindcheckcode   = $.trim($("#bindcheckcode").val());
		if(bindtelnum ==''){
			alert("请输入手机号！");
			return false;
		}
		if(checkTel(bindtelnum)){
			alert("手机号格式不对！");
			return false;
		}
		if(bindcheckcode==''){
			alert('验证码不能为空');
			return false;
		}
		$("#bindtelnum").attr("disabled","");
		$.post("/userInfo/bindMobile",
				{mobile:bindtelnum,validCode:bindcheckcode,telidentify:2},function(data){
					if(data==0){
						$("#layout").hide();
						//修改成功跳转
						setTimeout(function(){ location.href = "/userInfo"},10);
					}else if(data==2){
						alert('输入的手机号和不一致！');
					}else if(data==1){
						alert('验证码有误');
					}
				},
				"text"
		);
	}
}

function getBindCode(codeidy){
	if(codeidy==1){
		var bindtelnum = $.trim($("#bindtelnum").val());
		if(bindtelnum ==''){
			alert("请输入手机号！");
			return false;
		}
		if(checkTel(bindtelnum)){
			alert("手机号格式不对！");
			return false;
		}
		$("#getBindCode").attr("disabled","disabled");
		$.post(
				"/userInfo/send",
				{mobile:bindtelnum},
				function(data){
					if(data==1){
						alert("验证码已成功发送，请查收！");
						$("#getBindCode").attr("disabled","");
//						$("#newtelnum").attr("disabled","disabled");
					}else if(data==-1){
						alert("网络错误，请重发");
						$("#getBindCode").attr("disabled","");
					}
				},
				"json"
		);
	}else if(codeidy==2){
		var oldtelnum = $.trim($("#oldtelnum").val());
		if(oldtelnum ==''){
			alert("请输入原手机号！");
			return false;
		}
		if(checkTel(oldtelnum)){
			alert("原手机号格式不对！");
			return false;
		}

		var newtelnum = $.trim($("#newtelnum").val());
		if(newtelnum ==''){
			alert("请输入新手机号！");
			return false;
		}
		if(checkTel(newtelnum)){
			alert("新手机号格式不对！");
			return false;
		}

		if(oldtelnum == newtelnum) {
			alert("新手机号和原手机号一样！");
			return false;
		}
		$("#getchangeCode").attr("disabled","disabled");
		$.post(
				"/userInfo/send",
				{mobile:newtelnum},
				function(data){
					if(data==1){
						alert("验证码已成功发送，请查收！");
						$("#getchangeCode").attr("disabled","");
//						$("#newtelnum").attr("disabled","disabled");
					}else if(data==-1){
						alert("网络错误，请重发");
						$("#getchangeCode").attr("disabled","");
					}
				},
				"json"
		);
	}
}
//第一次手机登录验证
function firstTelLogin(){
	var firsttel = $("#firstlogintel").val();
	if(firsttel==''||checkTel(firsttel)){
		alert('您输入的手机有误');
		return false;
	}
	$.post("user.php?act=checktellogin2",{firsttel:firsttel},function(data){
		if(data.errno==1){
			window.location.href="user.php?act=tellogin3";
		}else if(data.errno==-1){
			alert('失败');
		}else if(data.errno==-2){
			alert('手机不存在');
		}
	},"json")
}
//找回密码
function getpwsbyemail(getiby){
	if(getiby==1){
		var useremail = $.trim($("#email").val());
		if(useremail==''|| checkEmail(useremail)){
			$("#passemcheck").html("<div>邮箱地址不合法</div>"); 
			return false;
		}
		$("#nexthtml").attr("disabled","disabled");
		$.post("user.php?act=check_forgetpw",{useremail:useremail,title:1},function(data){
			if(data.message==1){
				setTimeout(function(){ location.href = data.url;},10);
			}else if(data.message==-1){
				$("#passemcheck").html("<div>email不存在</div>");
				$("#nexthtml").attr("disabled","");
			}else if(data.message==-2){
				$("#passemcheck").html("<div>email发送失败</div>");
				$("#nexthtml").attr("disabled","");
			}else if(data.message==-3){
				$("#passemcheck").html("<div>请输入您注册时的email</div>");
				$("#nexthtml").attr("disabled","");
			}
		},"json");
	}else if(getiby==2){
		var usertel      = $("#usertel").val();
		var telcheckcode = $("#telcheckcode").val();
		if(usertel==''|| checkTel(usertel)){
			$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>请输入11位手机号码</em></span>"); 
			return false;
		}
		if(telcheckcode==''){
			$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>请输入手机验证码</em></span>"); 
			return false;
		}
		$("#telpassbutton").attr("disabled","disabled");
		$.post("user.php?act=check_forgetpw",{usertel:usertel,telcheckcode:telcheckcode,title:2},function(data){
			if(data.message==1){
				setTimeout(function(){ location.href = data.url;},10);
			}else if(data.message==-1){
				$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>手机验证码错误</em></span>");
				$("#telpassbutton").attr("disabled","");
			}
			else if(data.message==-2){
				$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>此手机并未注册</em></span>"); 
				$("#telpassbutton").attr("disabled","");
			}else if(data.message==-3){
				$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>请输入11位手机号码</em></span>"); 
				$("#telpassbutton").attr("disabled","");
			}
			else if(data.message==-4){
				$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>时间已过期请重新发送</em></span>");
				$("#telpassbutton").attr("disabled","");

			}
		},"json");
	}
}
//发送验证码到手机
function sendCheckCode(){
	var usertel      = $.trim($("#usertel").val());
	if(usertel==''|| checkTel(usertel)){
		$("#passtelcheck").html("<span class='redInfo allstep_info'><b></b><em>请输入11位手机号码</em></span>"); 
		return false;
	}
	$("#getcodebutton").attr("disabled","disabled");
	$.post(
			"user.php?act=sendcheckcode",
			{usertel:usertel},
			function(data){
				if(data.errno==1){
					$("#passtelcheck").html("<span class='yellowInfo allstep_info'><b></b><em>验证码已成功发送，请查收！</em></span>"); 
					updateTimer('showspan',60);
				}else if(data.errno==-1){
					$("#passtelcheck").html("<span class='yellowInfo allstep_info'><b></b><em>网络错误，请重发</em></span>");
					$("#getcodebutton").attr("disabled","");
				}else if(data.errno==-2){
					$("#passtelcheck").html("<span class='yellowInfo allstep_info'><b></b><em>此手机并未注册</em></span>"); 
					$("#getcodebutton").attr("disabled","");
				}
				else if(data.errno==-3){
					$("#passtelcheck").html("<span class='yellowInfo allstep_info'><b></b><em>请使用此账号绑定手机</em></span>"); 
					$("#getcodebutton").attr("disabled","");
				}
			},
			"json"
	);
}
//60秒倒计时
function updateTimer(showspan,timeval)
{
	if(timeval>=0){
		//timer = el("showspan")
		//timer.innerHTML = TimeToHMS(timeval);
		$("#" + showspan).html(timeval);
		timeval--;
		setTimeout("updateTimer('"+showspan +"', " + timeval+")",1000);
	}else{
		timeval=0;
		$("#" + showspan).html("<input type='button' class='allstep_yzm' onClick='sendCheckCode()' value='重新发送' />");
		//window.location.reload();
	}

}
//忘记登录密码 修改密码
function updatePass(){
	var idemail    = $.trim($("#idemail").val());
	var firstpass  = $("#upuserpass1").val();
	var secondpass = $("#upuserpass2").val();
	var usertel    = $("#upusertel").val();
	var upuserid   = $("#upuserid").val();
	if(firstpass==''|| checkAllPass(firstpass)){
		alert("请根据提示输入密码！");
		return false;
	}
	if(firstpass!=secondpass){
		$(".checkupuserpass2").html("<em>两次密码不一致</em>");
		return false;
	}
	$("input[@type=submit]").attr("disabled","disabled");
	$.post(
			"user.php?act=check_updatepass",
			{idemail:idemail,firstpass:firstpass,secondpass:secondpass,usertel:usertel,upuserid:upuserid},
			function(data){
				if(data.errno==1){
					alert('修改成功');
					window.location.href="user.php?act=login"; 
				}else if(data.errno==-1){
					$("input[@type=submit]").attr("disabled","");
					alert('修改失败');
				}
			},
			"json"
	);
}
//忘记交易密码 修改密码
function updateTradePass(){
	var passreg    = /^[0-9]*$/;
	var idemail    = $.trim($("#idemail2").val());
	var firstpass  = $("#uptradepass1").val();
	var secondpass = $("#uptradepass2").val();
	var usertel    = $.trim($("#upusertel2").val());
	var upuserid   = $("#upuserid2").val();
	if(firstpass==''|| checkAllPass(firstpass)||firstpass.length<6){
		return false;
	}
	if(firstpass!=secondpass){
		$(".checkupuserpass2").html("<em>两次密码不一致</em>");
		return false;
	}
	$("input[@type=submit]").attr("disabled","disabled");
	$.post(
			"user.php?act=check_updatepass",
			{idemail:idemail,firstpass:firstpass,secondpass:secondpass,usertel:usertel,upuserid:upuserid},
			function(data){
				if(data.errno==1){
					alert('修改成功');
				}else if(data.errno==-1){
					alert('修改失败');
					$("input[@type=submit]").attr("disabled","");
				}
			},
			"json"
	);
}
//关闭弹出层
function closediv(closeidy){
	if(closeidy==1){
		$("#changeusertel").hide();
		window.location.reload();
	}else if(closeidy==2){
		$("#bindusertel").hide();
		window.location.reload();
	}
}
//激活邮箱
function activationEmail(){

	$.post(
			"user.php?act=activationemail",
			{},
			function(data){
				if(data.errno==1){
					alert("已经发送激活邮件到您的邮箱，请注意查收");
					$("#jihuo").attr("onclick","");
					$("#jihuo").click(function(e){
						alert('对不起,您已经发送过邮箱激活邮件！')
					});
				}else{
					alert("发送失败");
				}
			},
			"json"
	);
}
/*弹出选定付款方式*/
function selectedPay() {
	window.location.href="togetherpay.php?or_id="+$("#orderinfo").val();
	/*
	var selectBrank_img = $("input[name=bank][checked]").parent().find('img').attr("src");
	$("#brank_img").attr("src",selectBrank_img);
	$.common.dialog({'id':'fk',"title":"付款方式"});
	 */
}

/*
 * 显示地址区域(修改时用)
 */
function region2(regId1,regId2,thisvalue){
	var reg_id = $("#" + regId1 + " option:selected").attr("regId");
	$.ajax({
		type: "POST",
		url: "user.php",
		cache:false,
		dataType:"json",
		data:"act=getRegion&reg_id="+reg_id+"&rand="+Math.random(),
		success: function(jn){
			$("#"+regId2).html("");
			$.each(jn, function(i){
				if(thisvalue==jn[i].REGION_NAME){
					$("<option regId='"+jn[i].REGION_ID+"' value='"+jn[i].REGION_NAME+"' selected>"+jn[i].REGION_NAME+"</option>").appendTo("#"+regId2);
				}else{
					$("<option regId='"+jn[i].REGION_ID+"' value='"+jn[i].REGION_NAME+"'>"+jn[i].REGION_NAME+"</option>").appendTo("#"+regId2);
				}
			});
			if(regId2!='f_consignee_addr'){
				region2('f_consignee_city','f_consignee_addr',thiscounty);
			}
		}
	});
}


//楠岃瘉鐪熷疄濮撳悕
function checkRealName(realname){
	var realnamereg     = /^([a-zA-Z\u4e00-\u9fa5]* ?)*[a-zA-Z\u4e00-\u9fa5]*$/;
	if(realname.match(realnamereg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉閭斂缂栫爜
function checkPostalcode(postalcode){
	var postalcodereg = /^[1-9]\d{5}$/;
	if(postalcode.match(postalcodereg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉鍥哄畾鐢佃瘽
function checkPhone(phone){
	var phonereg = /^0[1-9]{2,3}-[1-9]\d{5,7}$/;
	if(phone.match(phonereg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉鎵嬫満鍙风爜
function checkTel(tel){
	var telreg = /^((15)\d{9})$|^((13)\d{9})$|^((18)\d{9})$/i;
	if(tel.match(telreg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉閭
function checkEmail(email){

	var reg = /([\w-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([\w-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)/i;
	if(reg.test(email)){
		return false;
	}else{
		return true;
	}

}
//楠岃瘉鐧诲綍瀵嗙爜
function checkLoginPass(loginpass){
	var loginpassreg = /^(?![a-zA-Z]+$)(?![0-9]+$)[a-zA-Z0-9]{6,20}$/;
	if(loginpass.match(loginpassreg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉鐢熸棩鏍煎紡
function checkBDay(bday){
	var bdayreg = /^((((19|20)(([02468][048])|([13579][26]))-02-29))|((20[0-9][0-9])|(19[0-9][0-9]))-((((0[1-9])|(1[0-2]))-((0[1-9])|(1\d)|(2[0-8])))|((((0[13578])|(1[02]))-31)|(((01,3-9])|(1[0-2]))-(29|30)))))$/;
	if(bday.match(bdayreg) == null){
		return true;
	}else{
		return false;
	}
}
//楠岃瘉QQ鏍煎紡
function checkQQ(qq){
	var qqreg = /^\s*[.0-9]{5,10}\s*$/;
	if(qq.match(qqreg) == null){
		return true;
	}else{
		return false;
	}
}
//鍒嗛〉璺宠浆
function skippage(skipurl){
	var pagenum = $("input[@name='pageinput']").val();
	window.location=skipurl+pagenum;return false;
}
//楠岃瘉浼氬憳鐧诲綍瀵嗙爜鍜屼氦鏄撳瘑鐮�
function checkAllPass(loginpass){
	var loginpassreg = /^[0-9]{6,20}$/;
	if(loginpass.match(loginpassreg) == null){
		return true;
	}else{
		return false;
	}
}