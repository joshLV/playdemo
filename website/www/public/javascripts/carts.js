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
function reorder(goods_id,increment){
    var element = $("#num_" + goods_id);
    var last_num = $("#last_num_" + goods_id);

    var new_num = Number(last_num.val()) + increment;
    if(new_num <= 0){
        return;
    }
    $.post('/carts',
            {goodsId:goods_id,increment:increment},
            function(data){
                element.val(new_num);
                last_num.val(new_num);
                calItem(goods_id);
                refreshAmount();
            });
}
//计算单行的总价
function calItem(goods_id){
    var total_price = (new BigNumber($("#price_" + goods_id).text())).multiply($("#num_" + goods_id).val()).toString();
    $("#subtotal_" + goods_id).text(total_price.toString());
}
//计算订单总价
function refreshAmount(){
    var number = 0;
    var amount = new BigNumber("0");

    $("input[id^=check_goods_]").each(function(){
        if(!this.checked){
            return true;
        }
        var el_id = $(this).attr("id");
        var goods_id = el_id.substr(el_id.lastIndexOf("_") + 1);
        number += Number($("#num_" + goods_id).val());
        amount = amount.add($("#subtotal_" + goods_id).text());

    });

    //var number = 0;
    //$("input.num_input").each(function(){number += Number($(this).val())});
    $("#total_num").text(number);

    //var amount = new BigNumber("0");
    //$("span[id^=subtotal_]").each(function(){amount = amount.add($(this).text())});;
    $("#carts_amount").text(amount.toString());
}

$(window).load(
    function(){
        //点击+按钮
        $("a.add_box").click(function() {
            reorder($(this).attr("name"),1);
            return false;
        });
        //点击-按钮
        $("a.reduce_box").click(function() {
            reorder($(this).attr("name"),-1);
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
            $.ajax({
                type:'DELETE',
                url:'/carts/' + goods_id,
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
                    refreshAmount();
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
            $(this).click( function(){
                if(all_checked()){
                    set_all_select_all_checkbox(true);
                }else{
                    set_all_select_all_checkbox(false);
                }
                refreshAmount();
            })
        }); 


        //点击批量删除
        $("#batch_delete").click(function(){
            var checked=[]
            $("input[id^=check_goods_]").each(function(){
                if(this.checked){
                    var id_temp = $(this).attr("id");
                    checked.push(id_temp.substr(id_temp.lastIndexOf("_")+1));
                }
            });

            if(checked.length == 0) return;

            $.ajax({
                type:'DELETE',
                url:'/carts/' +checked.join(","),
                success:function(data){
                    for(var i = 0; i< checked.length; i++){
                        $("#row_" + checked[i]).remove();
                    }
                    set_all_select_all_checkbox(false);
                    refreshAmount();
                }});
            
            });
        set_all_select_all_checkbox(true);
        set_all_goods_checkbox(true);
        refreshAmount();
    }
);

