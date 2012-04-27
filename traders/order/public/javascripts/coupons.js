$(window).load(function(){
	  $("#eCouponSn").click(function() {
		  $("#checksn").html("");
		  $("#showinfo").html("");
		  $("#statusw").html("");
	  });
	
    $("#query").click(function() {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#shopId").val();
        if(eCouponSn ==""){
            $("#checksn").html("<font color=red>请输入券号!</font>");
            return false;
        }
        var shopName = $("#shopName").val();
        if (shopName == undefined) {
        	shopName = $("#shopId option:selected").text();
        }
        $.post(
                "/coupons/query",
                {shopId:shopId,eCouponSn:eCouponSn},
                function(data){
                    if(data !=null && data.error ==0 ){
                        if(data.status == "CONSUMED"){
                            $("#checksn").html("<font color=red>此券已消费!</font>");
                            $("#sure").attr("disabled",true);
                            var consumedAt=data.consumedAt;
                            $("#showinfo").html('券编号: '+data.eCouponSn+'<br />商品名称: '+data.name+'<br />消费日期：'+data.consumedAt);
                        }else if(data.status =="EXPIRED"){
                             $("#checksn").html("<font color=red>此券已过期!</font>");
                             $("#showinfo").html('券编号: '+data.eCouponSn+'<br />商品名称: '+data.name+'<br />截止日期：'+data.expireAt);
                        }else if(data.statu =="REFUND"){
                            $("#checksn").html("<font color=red>此券已经退款，无法再使用该券号进行消费!</font>");
                            $("#sure").attr("disabled",true);
                            $("#showinfo").html('券编号: '+data.eCouponSn+'<br />商品名称: '+data.name+'<br />退款日期：'+data.refundAt);
                        }else {
                        	 $("#checksn").html("");
                        	 $("#sure").attr("disabled",false);
                        	 $("#statusw").html('券状态:未消费');
                        	 $("#showinfo").html('券编号: '+data.eCouponSn+'<br />商品名称: '+data.name+'<br />截止日期：'+data.expireAt);
                        } 
                    } else if (data.error ==1) { 
                    	 $("#checksn").html("<font color=red>对不起，此券不能在 "+shopName+"门店使用!</font>");
                    } else if (data.error ==2) { 
                    	$("#checksn").html("<font color=red>对不起，只能在今天"+data.timeBegin+"~"+data.timeEnd+"时间内使用该券!</font>");
                    	$("#showinfo").html("");
                    } else{
                        $("#checksn").html("<font color=red>对不起，没有该券的信息!</font>");
                    }
                },
                "json"
            );
    });
    
    
    $("#sure").click(function() {
        var eCouponSn = $("#eCouponSn").val();
        var shopId = $("#shopId").val();
        var shopName = $("#shopName").val();
        if (shopName == undefined) {
        	shopName = $("#shopId option:selected").text();
        }
        if(eCouponSn ==""){
            $("#checksn").html("<font color=red>请输入券号!</font>");
            return false;
        }
         $.ajax({
             url: "/coupons/update",
             data:"shopId="+shopId+"&eCouponSn="+eCouponSn+"&shopName="+shopName,
             type: 'POST',
             error: function() { alert('消费失败!'); },
             success: function(data) {
                 if (data == '0') {
                     $("#checksn").html("<font color=red>该券消费成功！</font>");
                     $("#statusw").html('券状态:已消费');
                     $("#sure").attr("disabled",false);
                 } else {
                     alert("消费失败！");
                 }

             }
         });
    });
});
