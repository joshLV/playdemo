/**
 *
 * @author likang
 */

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
                    }
                    set_all_select_all_checkbox(false);
                }});
            
            });
    }
);

