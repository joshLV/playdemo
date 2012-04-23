/**
 * 商品列表页.
 * User: yjy
 * Date: 3/27/12
 * Time: 11:25 AM
 */

function buyNow(goodsId){
	$('#buy_now_form_'+goodsId).submit();
}

$(window).load(
		function () {
			/**
			 * 排序点击事件
			 */
			$(".sort_box").click(function () {
				var oldLink = $(this).attr('href');
				if (oldLink.split("-").length == 7) {//排序点击事件
					if ($(this).hasClass("selected_border")) {
						if ($(this).hasClass("box_sort_asc")) {
							$(this).removeClass("box_sort_asc");
							$(this).addClass("box_sort_desc");
							$(this).attr('href', oldLink + "-1");
						} else {
							$(this).removeClass("box_sort_desc");
							$(this).addClass("box_sort_asc");
							$(this).attr('href', oldLink + "-0");
						}
					} else {
						$(this).attr('href', oldLink + "-1");
					}
				}
			});

			/**
			 * 价格范围的确定点击事件
			 */
			$("#link_price_confirm").click(function () {
				var oldLink = $(this).attr('href');
				var linkItems = oldLink.split("-");
				var priceFrom = $("#input_price_from").val();
				if (!isNaN(parseInt(priceFrom)) && priceFrom >= 0) {
					linkItems[1] = priceFrom;
				} else {
					$("#input_price_from").val("");
					$("#input_price_from").focus();
					return false;
				}
				var priceTo = $("#input_price_to").val();
				if (!isNaN(parseInt(priceTo)) && priceTo > 0) {
					linkItems[2] = priceTo;
				} else {
					$("#input_price_to").val("");
					$("#input_price_to").focus();
					return false;
				}
				$(this).attr('href', linkItems.join("-"));
			});

			$("#selectall").click(function () {
				if (this.checked) {
					var obj =$("[name='goodsIds[]']").attr("disabled");
					if (!obj ) {
						$("[name='goodsIds[]']").attr("checked", 'true');//全选
					}
					
				} else {
					$("[name='goodsIds[]']").removeAttr("checked");//取消
				}
			});

			var checkedcnt = 0;
			$("#addto").click(function () {
				var goodsIds = [];
				$("input[name='goodsIds[]']").each(function () {
					if (this.checked) {
						checkedcnt++;
						goodsIds.push($(this).attr("value"));
					}
				});
				if (checkedcnt == 0) {
					alert("请至少选择一条数据！");
				} else {
					$.post("/library",{'goodsIds':goodsIds.join(",")},
					function (data) {
						$('#batchAdd').show();
						var len =0;
						if (data != null ) {
							ids = data.goodsId.split(",");
							len = ids.length;
						}
						for (var i=0;i<len;i++) {
							$('#goodsId_'+ids[i]).html(" <input type='checkbox' disabled/>");
							$('#add_'+ids[i]).html("已加入分销库");
							$('#libray_'+ids[i]).removeClass("font_blue");
						}
						//5秒后自动消失
						setTimeout('refeash()', 5000);
						
					});			
				}
			});
		}
);

function refeash(){
	$("#batchAdd").css("display","none");
}

/**
 *点击加入分销库按钮
 */
function addToLibray(goodsId) {

	$.post("/library",{'goodsIds':goodsId},
	function (data) {
     	$('#add_cart_result_'+data.goodsId).show();
     	$('#add_'+data.goodsId).html("已加入分销库");
		$('#libray_'+data.goodsId).removeClass("font_blue");
		$('#goodsId_'+data.goodsId).html(" <input type='checkbox' disabled/>");
		//5秒后自动消失
		setTimeout("display("+data.goodsId+")", 5000);
	});	
}

function display(goodsId){
	$("#add_cart_result_"+goodsId).css('display','none');
}

