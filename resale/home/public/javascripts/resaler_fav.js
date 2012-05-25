/**
 *
 * @author likang
 */
function cal_position_top(id){
    var st=document.documentElement.scrollTop;//滚动条距顶部的距离 
    var ch=document.documentElement.clientHeight;//屏幕的高度 
    var height=$("#"+id).height();//浮动对象的高度 
    return Number(st)+(Number(ch)-Number(height))/2; 
}
function cal_position_left(id){
    var sl=document.documentElement.scrollLeft;//滚动条距左边的距离 
    var cw=document.documentElement.clientWidth;//屏幕的宽度 
    var width=$("#"+id).width();//浮动对象的宽度
    return Number(sl)+(Number(cw)-Number(width))/2; 
}
function reset_result_dialog(){
    if($("#full_bg").css("display") == "block"){
        var body_height=document.documentElement.clientHeight;//屏幕的高度
        var body_width=document.documentElement.clientWidth;//屏幕的宽度
//        var body_height = $("body").height();
//        var body_width  = $("body").width();
        $("#full_bg").css({
            width:body_width,
            height:body_height});
        $("#payment_result").css({
            top:cal_position_top("payment_result"),
            left:cal_position_left("payment_result"),
            display:"block"});

    }
}
function close_result_dialog(){
    $("#full_bg").css({display:"none"});
    $("#payment_result").css({display:"none"});
}

$(window).load(
    function(){
        //点击删除
        $("a.delete_gift").click(function(){  
            var goods_id = $(this).attr("name");
            $.ajax({
                type:'DELETE',
                url:'/library/' + goods_id,
                success:function(data){
                    $("#row_" + goods_id).remove();
                    $("#row_hd_" + goods_id).remove();
                    $("#row_sep_" + goods_id).remove();
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
                url:'/library/' +checked.join(","),
                success:function(data){
                    for(var i = 0; i< checked.length; i++){
                        $("#row_" + checked[i]).remove();
                        $("#row_hd_" + checked[i]).remove();
                        $("#row_sep_" + checked[i]).remove();
                    }
                    set_all_select_all_checkbox(false);
                }});
            
            });
        
         $("#search").click(function () {
             $('#orderForm').submit();
         });
         
         //批量上传淘宝
         $(window).scroll(function(){reset_result_dialog()});
         $(window).resize(function(){reset_result_dialog()});

         $("#batch_upload_show").click(function(){
             $("#full_bg").css({display:"block"});
             reset_result_dialog();
             return false;
         });
         
         $("#batch_upload_btn").click(function(){
             var checked="";
             $("input[id^=check_goods_]").each(function(){
                 if(this.checked){
                     var id_temp = $(this).attr("id");
                     checked += id_temp.substr(id_temp.lastIndexOf("_")+1) + ",";
                 }
             });
             if(checked.length == 0) return;
             $("#batch_upload_goodsids").val(checked);
             $("#batch_upload_form").submit();
             return false;
         });
         
    }
);

