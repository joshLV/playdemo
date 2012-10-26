function append_questions(questions) {
    var q_size = Number($("#q-size").val());
    for (var i = 0; i < questions.length; i++) {
        $("#q-list").append('                                       \
                    <div class="question"><dl>                                      \
                            <dt>咨询内容：</dt>         \
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
        var addCartNumber = Number($("#addCartNumber").val())+element*1;
        var limitNumber = Number($("#limit_" + id).val());
        if (limitNumber > 0 && limitNumber - boughtNumber > 0 && addCartNumber > (limitNumber - boughtNumber)) {
            $(".error").html("<strong style='display: block;'>已经超过限购数量，不能继续加入购物车！</strong>").css('color', '#F55');
            return false;
        }

        if (limitNumber > 0 && element > (limitNumber - boughtNumber)) {
            element.val(limitNumber - boughtNumber);
            return false;
        }

        $.post(
            "/carts",
            {'goodsId':id, 'increment':$("#number").val()},
            function (data) {
                $("#addCartNumber").val(addCartNumber);
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

        $("#order_create_form").submit();
        return false;
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
        var mobile = $("#mobile").val();
        var validMobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/;
        if (mobile != "" && !validMobile.test(mobile)){
            alert("手机号码格式不正确");
            return false;
        }

        $.post(
            "/user-question",
            {"content":question,"mobile":mobile, "goodsId":$("#goodsId").val()},
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
        $("#stock_hit").css("display","inline-block");
        return;
    }else {
        $("#stock_hit").css("display","none");
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

 jQuery(function($) {
    // addCart
    $('.addCart b').click(function(e) {
        e.preventDefault();
        $('#add_cart_result').show();
    });
    $('#add_cart_result .close-tips').click(function(e) {
        e.preventDefault();
        $('#add_cart_result').hide();
    });
    // 主图切换
    $('#gallery').slides({
        play:5000,
        pause:2500,
        slideSpeed:500,
        hoverPause:true
    }).hover(
        function() {
            $('#gallery .btn').show();
        },
        function() {
            $('#gallery .btn').hide();
        }
    );
    // 右边广告切换
    $('#switch').slides({
        play:4000,
        pause:2000,
        slideSpeed:300,
        hoverPause:true
    });
    // 分享
    $('#qq').click(function(e) {
        e.preventDefault();
        $('#share-im').slideToggle(100);
    });
    $('#share-url').click(function(){
        $(this).select();
    });


    /**
     * 地图相关
     */
    var minGmap, bigGmap,
        outletShow = $('.outlet-show'),
        latlngStr = outletShow.attr('data-latlng'),
        latlngArr = latlngStr.split(',');

    var mapOpts = {
        latlngStr: latlngStr,
        latlng: new google.maps.LatLng(latlngArr[0], latlngArr[1]),
        title: outletShow.text()
    };

    function initialize(id, zoom, latlng, title) {
        var options = {
            zoom: zoom,
            center: latlng,
            zoomControl: true,
            panControl:  false,
            scaleControl: false,
            overviewMapControl: false,
            streetViewControl: false,
            mapTypeControl: false,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        },

        map = new google.maps.Map(document.getElementById(id), options),

        marker = new google.maps.Marker({
            position: latlng,
            map: map,
            animation: google.maps.Animation.DROP,
            title: title
        });
        return {
            map: map,
            marker: marker
        };
    }
    minGmap = initialize('min_gmap', 13, mapOpts.latlng, mapOpts.title);

    // 切换店面地址
    $('.outlet-name').live('click', function(){
        $('.outlet-name').removeClass('outlet-show');
        $(this).addClass('outlet-show');
        $('.outlet-attr:visible').slideUp(100);
        $(this).siblings().slideToggle(100);

        var str = $(this).attr('data-latlng'),
            arr = str.split(',');

        mapOpts.latlngStr = str;
        mapOpts.latlng = new google.maps.LatLng(arr[0], arr[1]);
        mapOpts.title = $(this).text();

        minGmap.map.setCenter(mapOpts.latlng);
        minGmap.marker.setMap(null);
        minGmap.marker = new google.maps.Marker({
            position: mapOpts.latlng,
            map: minGmap.map,
            animation: google.maps.Animation.DROP,
            title: mapOpts.title
        });
    });

    // 查看地图
    $('.view-map').live('click', function(e){
        e.preventDefault();

        if ($('#map_mask').length == 0) {
            $('body').append('<div id="map_mask"></div>');

            $('#map_mask').css({
                'width': $(window).width(),
                'height':  $('body').height()
            });
        } else {
            $('#map_mask').show();
        }
        if ($('#map_box').length == 0) {
            $('body').append(
                 '<div id="map_box">'
                +    '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'
                +    '<h3>' + mapOpts.title + '</h3>'
                +    '<div id="big_gmap" style="width:800px;height:500px;"></div>'
                +    '<p>提醒：地图标注位置仅供参考，具体情况以实际道路标识信息为准</p>'
                + '</div>');
            $('#map_box .close').click(function(){
                $('#map_box').hide();
                $('#map_mask').hide();
            });
        } else {
            $('#map_box').show();
            $('#map_box h3').text(mapOpts.title);
        }
        $('#map_box').css({
            'top': $(window).height()/2 - 284 + $(document).scrollTop() +'px',
            'left': $(window).width()/2 - 413 +'px'
        });

        if (bigGmap == undefined ) {
            bigGmap = initialize('big_gmap', 15, mapOpts.latlng, mapOpts.title);
        } else {
            bigGmap.map.setCenter(mapOpts.latlng);
            bigGmap.marker.setMap(null);
            bigGmap.marker = new google.maps.Marker({
                position: mapOpts.latlng,
                map: bigGmap.map,
                animation: google.maps.Animation.DROP,
                title: mapOpts.title
            });
        }
    });
    // 公交/驾车
    $('.search-path').live('click', function(e){
        e.preventDefault();
        if ($('#map_mask').length == 0) {
            $('body').append('<div id="map_mask"></div>');

            $('#map_mask').css({
                'width': $(window).width(),
                'height':  $('body').height()
            });
        } else {
            $('#map_mask').show();
        }
        if ($('#map_search').length == 0) {
            $('body').append(
                '<div id="map_search">'
                +   '<a class="close" href="javascript:void(0)" hidefocus="true"></a>'
                +   '<h3>查询路线</h3>'
                +   '<form action="http://ditu.google.cn/maps" method="get" target="_blank">'
                +      '<ul>'
                +          '<li><span class="text">目的地</span> <span id="daddr-txt">'+ mapOpts.title +'</span><input type="hidden" id="daddr-val" name="daddr" value="'+ mapOpts.latlngStr +'"></li>'
                +          '<li><span class="text">出行方式</span> <input type="radio" name="dirflg" checked value="r">公交 <input type="radio" name="dirflg" value="d">驾车</li>'
                +          '<li><span class="text">出发地</span> <input type="text" name="saddr" class="saddr"></li>'
                +          '<li><button type="submit" class="btn">查询</button></li>'
                +      '</ul>'
                +   '</form>'
                +'</div>');
            $('#map_search .close').click(function(){
                $('#map_mask').hide();
                $('#map_search').hide();
            });
        } else {
            $('#map_search').show();
            $('#daddr-txt').text(mapOpts.title);
            $('#daddr-val').val(mapOpts.latlngStr);
        }
        $('#map_search').css({
            'top': $(window).height()/2 - 100 + $(document).scrollTop() +'px',
            'left': $(window).width()/2 - 183 +'px'
        });
    });
    // 门店分页
    var currPage = 1,
        totalPage = Math.ceil($('#outlet-total-num').text()/5),
        outletPage = $('#outlet-page'),
        goodsId = $('#goodsId').val();
    function callback(data) {
        alert(data)
        var html = '';
        for (i in data) {
            html += '<li>'
                +'<h5 class="outlet-name" data-latlng="'+ data[i].latlng +'">'+ data[i].name +'</h5>'
                +'<div class="outlet-attr">'
                +'    <p>'+ data[i].addr +'</p>'
                +'    <p><span>'+ data[i].tel +'</span> <a class="view-map" href="#">查看地图»</a> <a class="search-path" href="#">公交/驾车»</a></p>'
                +'</div>'
            +'</li>';
        }
        $('.outlet-list ul').html(html);

        $('.outlet-attr:not(:first)').hide();
        $('.outlet-name:first').addClass('outlet-show');

        var str = data[0]['latlng'],
            arr = str.split(',');
        mapOpts.latlngStr = str;
        mapOpts.latlng = new google.maps.LatLng(arr[0], arr[1]);
        mapOpts.title = data[0]['name'];

        minGmap.map.setCenter(mapOpts.latlng);
        minGmap.marker.setMap(null);
        minGmap.marker = new google.maps.Marker({
            position: mapOpts.latlng,
            map: minGmap.map,
            animation: google.maps.Animation.DROP,
            title: mapOpts.title
        });
    }
    $('#outlet-page').delegate('a','click', function(e){
        e.preventDefault();
        if ($(this).hasClass('next-page')) {
            currPage++;
            $.getJSON('/goods/'+ goodsId +'/shops', 'currPage='+ currPage, function(data){
//                $("#shops").load( '/goods/'+ goodsId +'/shops', 'currPage='+ currPage, function(data){
//                callback(data);
                if ( totalPage <=2 ) {
                    outletPage.html('<a class="prev-page" href="#">上一页</a>');
                } else if (currPage == totalPage) {
                    outletPage.html('<a class="prev-page" href="#">上一页</a>');
                } else {
                    outletPage.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>');
                }
            });
        }
        if ($(this).hasClass('prev-page')) {
            currPage--;
            $("#shops").load( '/goods/'+ goodsId +'/shops', 'currPage='+ currPage, function(data){
//            $.getJSON('/yome/home/outletList.php', 'goods-id='+ goodsId +'opage='+ currPage, function(data){
//                callback(data);
                if (currPage == 1) {
                    outletPage.html('<a class="next-page" href="#">下一页</a>');
                } else {
                    outletPage.html('<a class="prev-page" href="#">上一页</a> | <a class="next-page" href="#">下一页</a>');
                }
            });
        }
    });
});
