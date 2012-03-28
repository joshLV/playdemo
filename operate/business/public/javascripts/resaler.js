function checkResaler(id,status,flg){
	var remark = $("#remark").val();
	if (flg == 0 && (remark == "" || remark== null)) {
		$("#checkRemark").html("请输入备注！");
	}else {
		$("#checkRemark").html("");
		var level= $("#level").val();
		var url="/resalers/update?id="+id+"&status="+status+"&level="+level+"&remark="+remark;
		$("#checkFrm").attr("method", "POST");
		$("#checkFrm").attr("action", url);
		$("#checkFrm").submit();
	}
}



