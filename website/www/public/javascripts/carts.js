function reorder(element,indent){
    var attr_id = element.attr("id");
    var idIndex = attr_id.indexOf("_");
    var goods_id = attr_id.substr(idIndex+1);
    var last_num = $("#last_num_"+goods_id);

    var new_num = Number(last_num.val()) + indent;
    if(new_num < 0){
        return;
    }
    $.post('/carts',
            {goodsId:goods_id,number:indent},
            function(data){
                element.val(new_num);
                last_num.val(new_num);
            });
}

$(window).load(
    function(){
        $("a.add_box").click(function() {
            reorder($("#num_"+$(this).attr("name")),1);
            return false;
        });
        $("a.reduce_box").click(function() {
            reorder($("#num_"+$(this).attr("name")),-1);
            return false;
        });
        $("input.num_input").blur(function(){
            var last_num = $("#last_"+$(this).attr("id"));
            var re= /^\d+(\.\d+)?$/;
            if(!re.test($(this).val())){
                $(this).val(last_num.val());
                return;
            }
            reorder($(this), Number($(this).val()) - Number(last_num.val()));
        });
        $("a.delete_gift").click(function(){  
            var goods_id = $(this).attr("name");
            $.post('/carts/del',
                {
                    goodsId:goods_id
                },
                function(data){
                    $("#row_" + goods_id).remove()
                });
            
            return false;
        });
    }
);

