function reorder(goods_id,indent){
    var element = $("#num_" + goods_id);
    var last_num = $("#last_num_" + goods_id);

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
    calItem(goods_id);
    refreshAmount();
}

function calItem(goods_id){
    var total_price = Number($("#price_" + goods_id).text()) * Number($("#num_" + goods_id).val());
    $("#subtotal_" + goods_id).val(total_price);
}

function refreshAmount(){
    var number = 0;
    $("input.num_input").each(function(){number += Number($(this).val())});
    $("#total_num").text(number);

    var amount = 0;
    $("input[id^=subtotal_]").each(function(){amount += Number($(this).val())});;
    $("#carts_amount").text(amount);
}

$(window).load(
    function(){
        $("a.add_box").click(function() {
            reorder($(this).attr("name"),1);
            return false;
        });
        $("a.reduce_box").click(function() {
            reorder($(this).attr("name"),-1);
            return false;
        });
        $("input.num_input").blur(function(){
            var el_id = $(this).attr("id");
            var last_num = $("#last_" + el_id);
            var re= /^\d+(\.\d+)?$/;
            if(!re.test($(this).val())){
                $(this).val(last_num.val());
                return;
            }
            var goods_id = el_id.substr(el_id.indexOf("_") + 1);

            reorder(goods_id, Number($(this).val()) - Number(last_num.val()));
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
        refreshAmount();
    }
);

