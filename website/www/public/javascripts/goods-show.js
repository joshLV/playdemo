function append_questions(questions) {
    var q_size = Number($("#q-size").val());
    for (var i = 0; i < questions.length; i++) {
        $("#q-list").append('                                       \
                    <div class="question"><dl>                                      \
                            <dt>' + questions[i].user + '</dt>         \
                            <dd>' + questions[i].content + '<span class="date">' + questions[i].date + '</span></dd>                      \
                        </dl></div>                                                    \
                        <div class="answer"> <dl>                                     \
                            <dt>待回复!</dt>                                    \
                            <dd>请耐心等待^_^</dd>                               \
                        </dl></div>                                                   \
                    ');

        q_size += 1;
    }

    $("#q-size").val(q_size);
}
$(function () {
    /**
     *点击加入购物车按钮
     */
    $("#link_add_cart").click(function () {
        var id = $("#goodsId").val();
        var element = $("#number").val();
        var boughtNumber = Number($("#boughtNumber").val());
        var addCartNumber = Number($("#addCartNumber").val());
        var limitNumber = Number($("#limit_" + id).val());
        if (limitNumber > 0 && limitNumber - boughtNumber > 0 && addCartNumber == (limitNumber - boughtNumber)) {
            $(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css('color', '#F55');
            return false;
        }

        if (limitNumber > 0 && element > (limitNumber - boughtNumber)) {
            element.val(limitNumber - boughtNumber);
            return false;
        }
//        var id = $("#goodsId").val();

        $.post(
            "/carts",
            {'goodsId':id, 'increment':$("#number").val()},
            function (data) {
                $("#addCartNumber").val(data.count);
                $('#add_cart_result').show();
                $('#add_cart_result').show();
                //5秒后自动消失
                setTimeout("$('#add_cart_result').css('display','none')", 5000);
                //显示最新的购物车信息
                $("#result-count").text(data.count);
                $("#result-amount").text(data.amount);

                //修改顶部购物车商品数量
                $("#cart-count").html(data.count);
                $("#reload").val("true");
                $("#order_confirm").hide();
            }
        );
    });

    $("#link_buy_more").click(function () {
        $('#add_cart_result').hide();
    });

    $("#link_buy_now").click(function () {
        var limitNumber = '${goods.limitNumber}';
        var number = Number($("#number").val());
        if (limitNumber > 0 && number > limitNumber) {
            $(".error").html("<strong>只能购买" + limitNumber + "个</strong>").css('color', '#F55');
            return false;
        }

        var t = $(this);
        t.attr("href", t.attr("href") + '-' + $("#number").val());
        return true;
    });

    $("#J_closeTips").click(function (ev) {
        ev.preventDefault();
        $('#add_cart_result').hide();
    });

    //提交问题
    $("#submit-question").click(function () {
        var question = $("#question").val();
        if (question.replace(/(^\s*)|(\s*$)/g, "") == "") {
            alert("请输入问题内容");
            return false;
        }
        $.post(
            "/user-question",
            {"content":question, "goodsId":$("#goodsId").val()},
            function (data) {
                if (data.error) {
                    $("#q-error").html(data.error).show();
                } else {
                    append_questions(data.questions)
                    $("#question").val("");
                    $("#q-error").hide().html("");
                }
            }
        ).error(function () {
                $("#q-error").html("网络错误").show();
            });
        return false;
    });
    //点击更多
    $("#show-more").click(function () {
        var max_result = Number($("#max-result").val())
        $.get(
            "/more-questions",
            {"goodsId":$("#goodsId").val(), "firstResult":max_result, "size":10},
            function (data) {
                append_questions(data.questions);
                $("#max-result").val(max_result + data.questions.length);
                if (data.questions.length < 10) {
                    $("#more-questions").css("display", "none");
                }
            }
        );
        return false;
    });
    // tab
    $('#tabbar li').click(function () {
        var that = $(this),
            attr = that.attr('data-id');

        $('#tabbar li').removeClass('curr');
        that.addClass('curr');

        if (attr == 'warmtips') {
            $('.tab-item').show();
        } else {
            $('.tab-item').hide();
            $('#' + attr + ' .hd').hide();
            $('#' + attr).show();
        }
    });


    //点击增加按钮
    $("#increase-btn").click(function () {
        reorder($(this).attr("name"), 1);
        return false;
    });
    //点击减少按钮
    $("#decrease-btn").click(function () {
        reorder($(this).attr("name"), -1);
        return false;
    });

});

function reorder(goods_id, increment) {
    var element = $("#number");
    var last_num_ele = $("#last_num_" + goods_id);
    var stock = Number($("#stock_" + goods_id).val());
    var limitNumber = Number($("#limit_" + goods_id).val());
    var last_num = Number(last_num_ele.val())
    var new_num = last_num + increment;
    var boughtNumber = Number($("#boughtNumber").val());
    if (new_num <= 0) {
        element.val(last_num);
        return;
    }
    if (new_num > 999) {
        new_num = 999;
        increment = 999 - last_num;
    }
    if (new_num > stock) {
        new_num = stock;
        increment = stock - last_num;
    }

    if (limitNumber > 0 && new_num > (limitNumber - boughtNumber)) {
        element.val(limitNumber - boughtNumber);
        increment = limitNumber - last_num;
        return false;
    }

    if (increment == 0) {
        element.val(last_num);
        return;
    }
    element.val(new_num);
    last_num_ele.val(new_num);
//    $.post('/carts',
//        {goodsId:goods_id, increment:increment},
//        function (data) {
//            element.val(new_num);
//            last_num_ele.val(new_num);
//        });
}

$("#link_add_cart").click(function () {
    var id = $("#goodsId").val();
    $.post(
        "/goods/statistics ",
        {'id':id, 'statisticsType':'ADD_CART'},
        function (data) {
            $('#summary_' + id).html(data);
        }
    );
});

