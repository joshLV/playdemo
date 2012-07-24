$(function() {
    // 添加、编辑收货地址
    $('#J_saveNewAddr').live('click', function(ev){
        ev.preventDefault();

        var addrId = $(this).attr('data-addrId'),
            addrName = $('#J_addrName'),
            addrPost = $('#J_addrPost'),
            addrStreet = $('#J_addrStreet'),
            addrMobile = $('#J_addrMobile'),
            addrPhone = $('#J_addrPhone');

        if ( addrName.val() == '' ) {
            $("#err-name").html('请填写您的真实姓名！');
            return;
        }
        if ( addrStreet.val() == '' ) {
            $("#err-street").html('请填写详细路名及门牌号！');
            return;
        }
        if ( !/^[1-9]\d{5}$/.test($.trim(addrPost.val())) ) {
            $("#err-post").html('请填写正确的邮政编码！');
            return;
        }
        if ( $.trim(addrMobile.val()) == '' && $.trim(addrPhone.val()) == '' ) {
            $("#err-phone").html("手机和电话请至少填写一个！");
            return;
        }
        var mobi = /^((15)\d{9})$|^((13)\d{9})$|^((18)\d{9})$/i;
        if ( !mobi.test($.trim(addrMobile.val())) && $.trim(addrPhone.val()) == '' ) {
            $("#err-phone").html("手机号码格式不正确！");
            return;
        }
        if ( $.trim(addrMobile.val()) == '' && !/^[0-9\-]{7,18}$/.test($.trim(addrPhone.val())) ) {
            $("#err-phone").html("座机号码格式不正确!");
            return;
        }
        if ( !addrId ) {
            $.post('/orders/addresses/new', {
                'address.name':$.trim(addrName.val()),
                'address.postcode':$.trim(addrPost.val()),
                'address.province':$('#J_addrProv').val(),
                'address.city':$('#J_addrCity').val(),
                'address.district':$('#J_addrDist').val(),
                'address.address':$.trim(addrStreet.val()),
                'address.mobile':$.trim(addrMobile.val()),
                'address.phoneNumber':$.trim(addrPhone.val()),
                'address.isDefault':true
            }, function (data) {
                $('#J_addr').html(data);
                $('#addressId').val(addrId);
            });
        } else {
            $.ajax({
                url:'/orders/addresses/' + addrId,
                type:'PUT',
                data:{
                    'address.id':addrId,
                    'address.name':$.trim(addrName.val()),
                    'address.postcode':$.trim(addrPost.val()),
                    'address.province':$('#J_addrProv').val(),
                    'address.city':$('#J_addrCity').val(),
                    'address.district':$('#J_addrDist').val(),
                    'address.address':$.trim(addrStreet.val()),
                    'address.mobile':$.trim(addrMobile.val()),
                    'address.phoneNumber':$.trim(addrPhone.val()),
                    'address.isDefault':true
                },
                success: function (data) {
                    $('#J_addr').html(data);
                    $('#addressId').val(addrId);
                }
            });
        }
    });
    $("input[class*='addr-']").focus(function(){
        $("span.error").html('');
    });

    // 删除
    $('.addr-del').live('click', function(ev){
        ev.preventDefault();

        var addrId = $(this).attr('data-addrid'),
            isDefault = $("#addrId_" + addrId).attr('checked') == 'checked';

        $('#J_addrEditBox').html('');
        $(this).parent().append('<div class="addr-del-confirm">您要删除该地址吗？<br><b>确定删除</b> <b>取消</b><img src="http://img.uhcdn.com/images/u/o_jian.png" /></div>');

        $('.addr-del-confirm').live('click', function(ev){
            var txt = $(ev.target).text();
            if (txt == '确定删除') {
                $.ajax({
                    url:'/orders/addresses/' + addrId,
                    type:'DELETE',
                    success:function() {
                        $('#addr-li-' + addrId).remove();
                        //如果被删除的地址是默认地址的话，则默认收货地址信息和地址列表都要重新加载
                        if (isDefault) {
                            $('#J_addrList').load('/orders/addresses/list');
                        }
                    }
                });
            } else if (txt == '取消') {
                $('.addr-del-confirm').remove();
            }
        });
    });

    // 编辑
    $('.addr-edit').live('click', function (ev) {
        ev.preventDefault();
        var addrId = $(this).attr('data-addrid');

        $('#J_addrEditBox').load('/orders/addresses/' + addrId + '/edit', function (data) {
            $("#addrId_" + addrId).attr('checked', 'checked');
            $('#J_saveAddr').hide();
        });
    });

    // 切换地址
    $('#J_addrAll input[name="addr"]').live('click', function() {
        $('#J_addrEditBox').html('');

        if ($(this).attr('id') == 'addrId_new') {
            $('#J_addrEditBox').load('/orders/addresses/new', function (data) {
                $('#J_saveAddr').hide();
            });
        } else {
            $('#J_saveAddr').show();
        }
    });

    // 确定地址
    $('#J_saveAddr').live('click', function(ev){
        var addrId = $('#J_addrAll input:checked').val();
        $.ajax({
            url:'/orders/addresses/' + addrId,
            type:'GET',
            success:function(data) {
                $('#J_addr').html(data);
                $('#addressId').val(addrId);
            }
        });
    });

    // 选择手机
    $('.mobi-item').click(function(){
        var _this = $(this);
        _this.addClass('selected');
        $('#ecart_mobile').val(_this.text());
        _this.siblings().removeClass('selected');
    });

    // 附加留言
    $('#J_explainHd').click(function(){
        $(this).addClass('minus');
        $('#J_explainText').show();
    });


    $("#J_confirmOrder b").click(function(ev) {
        ev.preventDefault();
        console.log(0);
       var url='/orders_number?items=' + $('#items').val();
        $.ajax({
            url:url,
            type:'GET',
            success:function (data) {
                if (data == '1') {
                   $("#limit").html("你所购买商品超过限购数量，请确认！");
                } else {
                    $("#order_create_form").submit();
                }
            }
        });

    });

    $('#J_modifyAddr').live('click', function(ev){
        ev.preventDefault();
        $('#J_addr').load('/orders/addresses', function(){});
    });

});
