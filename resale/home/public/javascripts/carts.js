/**
 *
 * @author likang
 */

/**
 * 发送订单数量变更请求，若数量变为非正整数，则拒绝操作
 *
 * @param goods_id  商品ID
 * @param increment 增量
 */
function reorder(goods_id,phone,increment){
    var element = $("#num_" + goods_id + "-" + phone);
    var last_num = $("#last_num_" + goods_id + "-" + phone);
    var stock = Number($("#stock_" + goods_id + "-" + phone).val());

    var new_num = Number(last_num.val()) + increment;
    if(new_num <= 0 || new_num > stock){
        element.val(last_num.val());
        return;
    }
    $.post('/carts/ajax',
            {goodsId:goods_id,increment:increment, phone: phone},
            function(data){
                element.val(new_num);
                last_num.val(new_num);
                calItem(goods_id + "-" + phone);
                refreshAmount();
            });
}
//计算单行的总价
function calItem(goods_id){
    var total_price = (new BigNumber($("#price_" + goods_id).text(), "2" )).multiply($("#num_" + goods_id).val());
    $("#subtotal_" + goods_id).text(total_price.toString());
}
//计算订单总价
function refreshAmount(){
    var number = 0;
    $("input.num_input").each(function(){number += Number($(this).val())});
    $("#total_num").text(number);

    var amount = new BigNumber("0");
    $("span[id^=subtotal_]").each(function(){amount = amount.add($(this).text())});;
    $("#carts_amount").text(amount.toString());
}

$(window).load(
    function(){
        //点击+按钮
        $("a.add_box").click(function() {
            reorder($(this).attr("iden"), $(this).attr("phone"),1);
            return false;
        });
        //点击-按钮
        $("a.reduce_box").click(function() {
            reorder($(this).attr("iden"), $(this).attr("phone"),-1);
            return false;
        });
        //直接在文本框里输入
        $("input.num_input").blur(function(){
            var el_id = $(this).attr("id");
            var last_num = $("#last_" + el_id);
            var re= /^\d+(\.\d+)?$/;
            if(!re.test($(this).val())){
                $(this).val(last_num.val());
                return;
            }
            var goods_id = el_id.substr(el_id.lastIndexOf("_") + 1);

            reorder(goods_id, Number($(this).val()) - Number(last_num.val()));
        });
        //点击删除
        $("a.delete_gift").click(function(){  
            var goods_id = $(this).attr("name");
            var phone = $("phone_" + goods_id).val();
            alert(phone);
            $.ajax({
                type:'DELETE',
                url:'/carts/' + goods_id + "-" + phone,
                success:function(data){
                    $("#row_" + goods_id).remove()
                    refreshAmount();
                }});
            
            return false;
        });

        var set_all_select_all_checkbox =function(checked){
            $("input[name=select_all_checkbox]").each(function(){
                this.checked = checked
            });
        };
        var set_all_goods_checkbox = function(checked){
            $("input[id^=check_goods_]").each(function(){
                this.checked = checked
            }); 
        };
        //点击全选
        $("input[name=select_all_checkbox]").each(function(){
               $(this).click( 
                  function(){ 
                     if(this.checked){ 
                        set_all_select_all_checkbox(true);
                        set_all_goods_checkbox(true);
                     }else{ 
                        set_all_select_all_checkbox(false);
                        set_all_goods_checkbox(false);
                     } 
                  } 
                )});
        //点击单个复选框
        var all_checked= function(){
            var all_check = true;
            $("input[id^=check_goods_]").each(function(){
                if(this.checked){
                    return true;
                }else{
                    all_check = false;
                    return false;
                }
            }); 

            return all_check;
        };
        $("input[id^=check_goods_]").each(function(){ 
            $(this).click(
                function(){
            if(all_checked()){
                set_all_select_all_checkbox(true);
            }else{
                set_all_select_all_checkbox(false);
            }
        })}); 


        //点击批量删除
        $("a[id^=batch_del_]").each(function(){
        	$(this).click(function(){
        		var id_temp = $(this).attr("id");
                id_temp = id_temp.substr(id_temp.lastIndexOf("_")+1);
        		var form = $("<form></form>");
        		form.attr('action','/carts/form-batch-delete');
        		form.attr('method','post');
        		var input = $("<input type='hidden' name='goodsIds[]' />");
        		input.attr('value', [id_temp]);
        		form.append(input);
        		form.appendTo("body");
        		form.submit();
        		return false;
        	});
        });
        
            	
/*
            $.ajax({
                type:'DELETE',
                url:'/carts/form-batch-delete',
                data:{'goodsIds[]':checked},
                success:function(data){
                    for(var i = 0; i< checked.length; i++){
                        $("#row_" + checked[i]).remove();
                    }
                    set_all_select_all_checkbox(false);
                    refreshAmount();
                }});
*/
            
        
        
            var first_opt = $("#favs_list option:first");
            if(first_opt.val() != '-1'){
                $("#goods_detail_" + first_opt.val()).css({"display":"block"});
            }
            $("#favs_list").change(function() {
                $("tr[id^=goods_detail]").each(function(){
                    $(this).css({"display":"none"});
                });
                $("#favs_list option:selected").each(function(){
                    $("#goods_detail_" + $(this).val()).css({"display": "block"});
                });
            });
        refreshAmount();
    }
);

