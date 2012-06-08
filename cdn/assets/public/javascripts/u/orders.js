$(function ($) {
    $('#J_modifyAddr').click(function (ev) {
        ev.preventDefault();
        var _this = $(this);

        if (_this.text() == '[修改]') {
            $("#err-addressInfo").hide();
            _this.text('[关闭]');
            $('#J_addrList').load('/Addresses/index', function () {
                $('#J_addrAll').show(100);
            });
            $('#J_addrEditBox').html("");
        } else {
            _this.text('[修改]');
            $('#J_addrAll').hide();
            $('#J_useAddr').hide();
            $('#J_confirm').text('送到这个地址').removeAttr('data-action');
        }
    });

    // 切换地址
    $('#J_addrAll ul input').live('click', function () {
        $('#J_addrEditBox').html("");

        if ($(this).attr('id') == 'addrId_new') {
            $('#J_addrEditBox').load('/orders/addresses/new', function (data) {
                $('#J_confirm').text('保存并送到这个地址').attr('data-action', 'add-addr');
            });
        } else {
            var thisid = $(this).attr('id'),
                thisaddr = $('label[for=' + thisid + ']').html();
            $('#J_addrCurrent').html(thisaddr);
            $('#J_confirm').text('送到这个地址');
        }
    });

    // 删除地址
    $('.addr-del').live('click', function (ev) {
        ev.preventDefault();

        var addrId = $(this).attr('data-addrid');
        var isDefault = $("#addrId_" + addrId).attr('checked') == 'checked';

        $(this).append('<div class="addr-del-confirm">您要删除该地址吗？<br><b>确定删除</b> <b>取消</b><img src="http://img.uhcdn.com/images/u/o_jian.png" /></div>');

        $('.addr-del-confirm').live('click', function (ev) {
            var txt = $(ev.target).text();
            if (txt == '确定删除') {
                $.ajax({
                    url:'/orders/addresses/' + addrId,
                    type:'DELETE',
                    success:function () {
                        $('#addr-li-' + addrId).remove();
                        //如果被删除的地址是默认地址的话，则默认收货地址信息和地址列表都要重新加载
                        if (isDefault) {
                            $('#J_addrCurrent').load('/orders/addresses/default');
                            $('#J_addrList').load('/orders/addresses');
                        }
                    }
                });
            } else if (txt == '取消') {
                $(this).remove();
            }
        });
    });

    // 编辑地址
    $('.addr-edit').live('click', function (ev) {
        ev.preventDefault();
        var addrId = $(this).attr('data-addrid');

        $('#J_addrEditBox').load('/orders/addresses/' + addrId + '/edit', function (data) {
            $("#addrId_" + addrId).attr('checked', 'checked');

            $('#J_confirm').text('保存并送到这个地址').attr({'data-action':'edit-addr', 'data-addrid':addrId});
        });
    });

    // 添加、修改地址
    $('#J_confirm').click(function (ev) {
        ev.preventDefault();

        var addrId = $(this).attr('data-addrid');

        if ($(this).text() == '保存并送到这个地址') { //新增地址时
            if ($(this).attr('data-action') == 'add-addr') {

                var addrName = $('#J_addrName'),
                    addrPost = $('#J_addrPost'),
                    addrStreet = $('#J_addrStreet'),
                    addrMobile = $('#J_addrMobile'),
                    addrAreaCode = $('#J_addrAreaCode'),
                    addrPhoneNum = $('#J_addrPhoneNum'),
                    addrPhoneExt = $('#J_addrPhoneExt');

                if (addrStreet.val() == '') {
                    $("#err-address").html('街道地址不能空！');
                    return;
                }
                if (!/^[1-9]\d{5}$/.test($.trim(addrPost.val()))) {
                    $("#err-postcode").html('邮政编码格式不对！');
                    return;
                }
                if (addrName.val() == '') {
                    $("#err-name").html('收货人不能空！');
                    return;
                }
                if (!/^[1-9]\d{10}$/.test($.trim(addrMobile.val()))) {
                    if (!/^\d{3,4}$/.test($.trim(addrAreaCode.val())) || !/^\d{7,8}$/.test($.trim(addrPhoneNum.val()))) {
                        $("#err-phone").html("联系电话填写不对!");
                        return;
                    }
                }
                $.post('/orders/addresses/new', {
                    'address.name':$.trim($('#J_addrName').val()),
                    'address.postcode':$.trim(addrPost.val()),
                    'address.province':$('#J_addrProv').val(),
                    'address.city':$('#J_addrCity').val(),
                    'address.district':$('#J_addrDist').val(),
                    'address.address':$('#J_addrStreet').val(),
                    'address.mobile':$.trim($('#J_addrMobile').val()),
                    'address.areaCode':$.trim($('#J_addrAreaCode').val()),
                    'address.phoneNumber':$.trim($('#J_addrPhoneNum').val()),
                    'address.phoneExtNumber':$.trim($('#J_addrPhoneExt').val()),
                    'address.isDefault':true
                }, function (data) {
                    $('#J_addrCurrent').load('/orders/addresses/default');
                    $('#J_modifyAddr').text('[修改]');
                    $('#J_addrAll').hide();

                    $('#J_addrAll ul').prepend(data);
                });

            } else if ($(this).attr('data-action') == 'edit-addr') { //修改地址时

                var addrName = $('#J_addrName'),
                    addrPost = $('#J_addrPost'),
                    addrStreet = $('#J_addrStreet'),
                    addrMobile = $('#J_addrMobile'),
                    addrAreaCode = $('#J_addrAreaCode'),
                    addrPhoneNum = $('#J_addrPhoneNum'),
                    addrPhoneExt = $('#J_addrPhoneExt');

                if (addrStreet.val() == '') {
                    $("#err-address").html('街道地址不能空！');
                    return;
                }
                if (!/^[1-9]\d{5}$/.test($.trim(addrPost.val()))) {
                    $("#err-postcode").html('邮政编码格式不对！');
                    return;
                }
                if (addrName.val() == '') {
                    $("#err-name").html('收货人不能为空！');
                    return;
                }
                if (!/^[1-9]\d{10}$/.test($.trim(addrMobile.val()))) {
                    if (!/^\d{3,4}$/.test($.trim(addrAreaCode.val())) || !/^\d{7,8}$/.test($.trim(addrPhoneNum.val()))) {
                        $("#err-phone").html("联系电话填写不对!");
                        $("#err-phone").html("联系电话填写不对!");
                        return;
                    }
                }
                $.ajax({
                    url:'/orders/addresses/' + addrId,
                    type:'PUT',
                    data:{
                        'address.id':addrId,
                        'address.name':$.trim($('#J_addrName').val()),
                        'address.postcode':$.trim(addrPost.val()),
                        'address.province':$('#J_addrProv').val(),
                        'address.city':$('#J_addrCity').val(),
                        'address.district':$('#J_addrDist').val(),
                        'address.address':$('#J_addrStreet').val(),
                        'address.mobile':$.trim($('#J_addrMobile').val()),
                        'address.areaCode':$.trim($('#J_addrAreaCode').val()),
                        'address.phoneNumber':$.trim($('#J_addrPhoneNum').val()),
                        'address.phoneExtNumber':$.trim($('#J_addrPhoneExt').val()),
                        'address.isDefault':true
                    },
                    success:function (data) {
                        $('#J_addrCurrent').html(data);
                        $('#J_modifyAddr').text('[修改]');
                        $('#J_addrAll').hide();
                        $('#J_addrAll label[for="addrId_' + addrId + '"]').html(data);
                        $('#J_addrAll input:checked').removeAttr('checked');
                        $('#addrId_' + addrId).attr('checked', 'checked');
                        $('#J_addrEditBox table').remove();
                        $('#J_confirm').text('送到这个地址').removeAttr('data-action');
                    }
                });
            }
        }
        else if ($(this).text() == '送到这个地址') {
            $('#addressId').val(addrId);
            $('#J_modifyAddr').text('[修改]');
            $('#J_addrAll').hide();
            var checkedId = $('#J_addrAll input:checked').attr('id'),
                checkHTML = $('#J_addrAll label[for="' + checkedId + '"]').html();
            $.ajax({
                url:'/orders/addresses/' + $('#J_addrAll input:checked').attr("value") + "/default",
                type:'PUT',
                data:{},
                success:function (data) {
                }
            });
            $('#J_addrCurrent').html(checkHTML);
        }
    });

    $("#link_confirm_pay").click(function () {
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


});
