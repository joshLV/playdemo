var lastUpdateAddressId = -1;

$.ready(function () {
    $("#china_area").jChinaArea();
});
/**
 * 添加地址行显示到地址列表中
 * @param data
 */
function addAddressToList(data) {
    var useNewAddressLi = $('#use_new_address_li');
    var addAddressLi = $("<li class='li_address_modify' id='li_address_'" + data.id + "' addressId='" + data.id + "'>" + data + "</li>");

    useNewAddressLi.before(addAddressLi);
}

function mergeAddress(province, city, district, address) {
    var fullAddress = "";
    if (province != 'ALL' && province != "" && province != undefined) {
        fullAddress += province;
    }
    if (city != "ALL" && city != "" && city != undefined) {
        fullAddress += " " + city;
    }
    if (district != "ALL" && district != "" && district != undefined) {
        fullAddress += " " + district;
    }
    if (fullAddress != "") {
        fullAddress += " ";
    }
    if (address != "") {
        fullAddress += address;
    }
    return fullAddress;
}

function mergePhone(mobile, areaCode, phoneNumber, phoneExtNumber) {
    var phone = "";

    if (areaCode != null && areaCode != "") {
        phone += areaCode + "-";
    }
    if (phoneNumber != null && phoneNumber != "") {
        phone += phoneNumber;
    }
    if (phoneExtNumber != null && phoneExtNumber != "") {
        phone += "-" + phoneExtNumber;
    }
    if (mobile != null && mobile != "") {
        phone = mobile + "   " + phone;
    }
    return phone;
}

function setAddress(addressId) {
    if (addressId == 0) {//如果是新增的地址

        $("#show_name").html($("#address_name").val());
        var province = $("select[name='address.province']").val();
        var city = $("select[name='address.city']").val();
        var district = $("select[name='address.district']").val();
        var address = $("#address_address").val();
        var fullAddress = mergeAddress(province, city, district, address);
        $("#show_address").html(fullAddress);

        $("#show_postcode").html($("#address_postcode").val());
        var areaCode = $("#address_areaCode").val();
        var mobile = $("#address_mobile").val();
        var phoneNumber = $("#address_phoneNumber").val();
        var phoneExtNumber = $("#address_phoneExtNumber").val();

        $("#show_phone").html(mergePhone(mobile, areaCode, phoneNumber, phoneExtNumber));
    } else {//数据库中原有的地址
        $("#addressId").val(addressId);
        $("#show_name").html($("#address_name_" + addressId).html());
        $("#show_address").html($("#address_address_" + addressId).html());
        $("#show_postcode").html($("#address_postcode_" + addressId).html());

        $("#show_phone").html($("#address_phone_" + addressId).html());
    }
}
/**
 * 显示地址详细信息
 */
function showAddress(show) {
    if (show) {
        $("#list_address_ul").hide();
        $("#show_address_ul").show();
        $("#div_add_address").hide();
    } else {
        $("#list_address_ul").show();
        $("#show_address_ul").hide();
        $("#div_add_address").show();
    }
}

/**
 * 显示新增地址的输入框
 */
function showCreateAddressEdit() {
    if (lastUpdateAddressId > 0) {
        $("#li_address_" + lastUpdateAddressId).load("/orders/addresses/" + lastUpdateAddressId, "", function (data) {
            $("#china_area").jChinaArea({aspnet:false, s1:"上海市", s2:"上海市", s3:"黄浦区"});
            $("#radio_address_0").attr("checked", true);
            $("#div_add_address").show();
            $("#bottom_buttons").show();
            lastUpdateAddressId = -1;
        });
    } else {
        $("#china_area").jChinaArea({aspnet:false, s1:"上海市", s2:"上海市", s3:"黄浦区"});
        $("#radio_address_0").attr("checked", true);
        $("#div_add_address").show();
        $("#bottom_buttons").show();
    }
}

/**
 * 初始化地址层的显示和隐藏
 *
 * 1、判断是否已有默认地址，如果有，只显示默认地址，不显示地址列表
 * 2、判断是否显示新增地址的输入框
 */
function initAddress() {
    var addressId = $("input[name='selectedAddressId']:checked").val();
    setAddress(addressId);
    if (addressId == 0) {
        $("#list_address_ul").show();
        $("#show_address_ul").hide();
        $("#div_add_address").show();
        $("#link_cancel").hide();
    }
    else {
        $("#list_address_ul").hide();
        $("#show_address_ul").show();
        $("#div_add_address").hide();
    }
    //设置当前用户的收货地址信息
}

function updateAddress(addressId) {
    $.ajax({
        type:'PUT',
        data:{
            'address.id':addressId,
            'address.name':$("#address_name_" + addressId).val(),
            'address.postcode':$("#address_postcode_" + addressId).val(),
            'address.province':$("#address_province_" + addressId).val(),
            'address.city':$("#address_city_" + addressId).val(),
            'address.district':$("#address_district_" + addressId).val(),
            'address.address':$("#address_address_" + addressId).val(),
            'address.mobile':$("#address_mobile_" + addressId).val(),
            'address.areaCode':$("#address_areacode_" + addressId).val(),
            'address.phoneNumber':$("#address_phoneNumber_" + addressId).val(),
            'address.phoneExtNumber':$("#address_phoneExtNumber_" + addressId).val(),
            'address.isDefault':'true'},
        url:'/orders/addresses/' + addressId,
        success:function (data) {
            //将编辑框重新设置为li
            $("#li_address_" + addressId).html(data);
            //设置当前用户的收货地址信息
            setAddress(addressId);
            showAddress(true);
        }});
}

function editAddress(addressId) {
    $("#li_address_" + addressId).load("/orders/addresses/" + addressId + "/edit", function (data) {
        if (lastUpdateAddressId > 0) {
            $("#li_address_" + lastUpdateAddressId).load("/orders/addresses/" + lastUpdateAddressId, "", function (data) {
                setAreaValue(addressId);
                $("#bottom_buttons").hide();
                $("#radio_address_" + addressId).attr("checked", true);
                $("#div_add_address").hide();
            });
        } else {
            setAreaValue(addressId);
            $("#bottom_buttons").hide();
            $("#radio_address_" + addressId).attr("checked", true);
            $("#div_add_address").hide();
        }
        lastUpdateAddressId = addressId;
    });
}

function deleteAddress(addressId) {
    if (confirm("您确定要删除这个地址吗?")) {
        $.ajax({
            type:'DELETE',
            url:'/orders/addresses/' + addressId,
            success:function (data) {
                $("#li_address_" + addressId).remove();
                $("#radio_address_" + data).attr("checked", true);
                if (data == "") {
                    $("#list_address_ul").show();
                    $("#show_address_ul").hide();
                    $("#div_add_address").show();
                    $("#link_cancel").hide();
                    $("#radio_address_0").attr("checked", true);
                }
            }});
    }
}

/**
 * 选择地址的单选框点击事件
 */
function radioClick() {

    var addressId = $("input[name='selectedAddressId']:checked").val();
    if (lastUpdateAddressId > 0) {
        $("#li_address_" + lastUpdateAddressId).load("/orders/addresses/" + lastUpdateAddressId, "", function (data) {
            $("#china_area").jChinaArea({aspnet:false, s1:"上海市", s2:"上海市", s3:"黄浦区"});
            $("#radio_address_" + addressId).attr("checked", true);
            $("#bottom_buttons").show();
            lastUpdateAddressId = addressId;
        });
    }
    $("#div_add_address").hide();

}

function setAreaValue(addressId) {
    $("#china_area").jChinaArea({aspnet:false, s1:$("#address_province_" + addressId).attr("selectedValue"), s2:$("#address_city_" + addressId).attr("selectedValue"), s3:$("#address_district_" + addressId).attr("selectedValue")});
}
$(window).load(
    function () {
        /**
         * 地址表格的 行点击事件
         */
//        $(".li_address_modify").click(function (event) {
//            if (event.srcElement == undefined) {
//                return;
//            }
//            var src = event.srcElement.tagName.toLowerCase();
//            if (src == 'input' || src == 'select' || src == 'a' || src == 'span') { //过滤修改时的控件，使这些控件不执行行点击事件
//                return;
//            }
//            addressId = $(this).attr('addressId');
//            $("#radio_address_" + addressId).attr("checked", true);
//
//            $("#div_edit_address_" + addressId).hide();
//            $("#div_add_address").hide();
//            alert("li_address_modify");
//        });

        /**
         * 点击确认收货信息按钮.
         */
        $("#link_confirm").click(function () {
            //添加收货地址信息到数据库中
            var addressId = $("input[name='selectedAddressId']:checked").val();
//            var addNew = addressId == 0;
            var addNew = $("#div_add_address").show();
            if (addNew) {
                $.post("/orders/addresses/new",
                    {selectedAddressId:$("#selectedAddressId").val(),
                        'address.name':$("#address_name").val(),
                        'address.postcode':$("#address_postcode").val(),
                        'address.province':$("#address_province").val(),
                        'address.city':$("#address_city").val(),
                        'address.district':$("#address_district").val(),
                        'address.address':$("#address_address").val(),
                        'address.mobile':$("#address_mobile").val(),
                        'address.areaCode':$("#address_areaCode").val(),
                        'address.phoneNumber':$("#address_phoneNumber").val(),
                        'address.phoneExtNumber':$("#address_phoneExtNumber").val(),
                        'address.isDefault':true
                    },
                    function (data) {
                        //设置form中的addressId
                        $("#addressId").val(data.id);
                        //在收货地址列表中增加一行
                        addAddressToList(data);
                    });

            } else {//修改用户的默认地址设置
                $.ajax({
                        type:'PUT',
                        url:'/orders/addresses/' + addressId + "/default",
                        success:function (data) {

                        }}
                );
            }
            //设置当前用户的收货地址信息
            setAddress(addressId);
            showAddress(true);
        });

        $("#link_cancel").click(function () {
            showAddress(true);
        });

        /**
         * 确认后的地址信息修改点击事件
         */
        $("#link_edit_show_address").click(function () {
            $("#list_address_ul").show();
            $("#show_address_ul").hide();
            $("#div_add_address").hide();
            $("#link_cancel").show();
            var addressId = $("#addressId").val();
            $("#radio_address_" + addressId).attr("checked", true);
            $("#li_address_" + addressId).load("/orders/addresses/" + addressId + "/edit", function (data) {
                setAreaValue(addressId);
                $("#bottom_buttons").hide();
                $("#radio_address_" + addressId).attr("checked", true);
                $("#div_add_address").hide();
                lastUpdateAddressId = addressId;
            });
        });

        /**
         * form提交事件
         */
        $("#link_confirm_pay").click(function () {
            $("#order_create_form").submit();
        });
    }
);



$(function(){
    $('#J_modifyAddr').click(function(ev){
        ev.preventDefault();
        if ($(this).text() == '[修改]') {
            $(this).text('[关闭]');
            $('#J_addrAll').show();
        } else {
            $(this).text('[修改]');
            $('#J_addrAll').hide();
            $('#J_useAddr').hide();
            $('#J_confirm').text('送到这个地址').removeAttr('data-action');
            $('#J_addrEditBox table').remove();
        }
    });

    // 切换地址
    $('#J_addrAll ul input').live('click', function(){
        $('#J_editAddr').hide();

        if ($(this).attr('id') == 'addrId_new') {
            $('#J_useAddr').show();
            $('#J_confirm').text('保存并送到这个地址').attr('data-action', 'add-addr');
        } else {
            $('#J_useAddr').hide();
            $('#J_confirm').text('送到这个地址');
        }
    });

    // 省市区三级联动
    new PCAS('addr-prov','addr-city','addr-dist','上海市','市辖区','黄浦区');

    // 删除地址
    $('.addr-del').live('click', function(ev){
        ev.preventDefault();

        var addrId = $(this).attr('data-addrid');
        $(this).append('<div class="addr-del-confirm">您要删除该地址吗？<br><b>确定删除</b> <b>取消</b><img src="http://img.uhcdn.com/images/u/o_jian.png" /></div>');

        $('.addr-del-confirm').click(function(ev){
            var txt = $(ev.target).text();
            if (txt == '确定删除') {
                $.ajax({
                    url: '/orders/addresses/'+ addrId,
                    type: 'DELETE',
                    success: function(){
                        $('#addr-li-'+ addrId).remove();
                    }
                });
            } else if (txt == '取消') { 
                $(this).remove();
            }
        });
    });

    // 编辑地址
    $('.addr-edit').live('click', function(){
        var addrId = $(this).attr('data-addrid');

        $('#J_addrEditBox').load('/orders/addresses/'+ addrId +'/edit', function(){
            $('#J_useAddr').hide();
            $('#J_editAddr').show();
            $('#J_confirm').text('保存并送到这个地址').attr({'data-action': 'edit-addr', 'data-addrid': addrId});
        });
    });

    // 添加、修改地址
    $('#J_confirm').click(function(ev){
        ev.preventDefault();

        var addrId = $(this).attr('data-addrid');

        if ($(this).text() == '保存并送到这个地址') {
            if ($(this).attr('data-action') == 'add-addr') {

                var addrName = $('#J_addrName'),
                    addrPost = $('#J_addrPost'),
                    addrStreet = $('#J_addrStreet'),
                    addrMobile = $('#J_addrMobile'),
                    addrAreaCode = $('#J_addrAreaCode'),
                    addrPhoneNum = $('#J_addrPhoneNum'),
                    addrPhoneExt = $('#J_addrPhoneExt');
                
                if (addrStreet.val() == '') {
                    addrStreet.parent().append('<span class="required"> 街道地址不能空！</span>');
                    return;
                }
                if (addrPost.val() == '' || !$.isNumeric(addrPost.val()) || !(addrPost.val().length == 6)) {
                    addrPost.parent().append('<span class="required"> 邮政编码格式不对！</span>');
                    return;
                }
                if (addrName.val() == '') {
                    addrName.parent().append('<span class="required"> 收货人不能空！</span>');
                    return;
                }
                if (addrMobile.val() == '' || !$.isNumeric(addrMobile.val()) || !(addrMobile.val().length == 11)) {
                    if (addrAreaCode.val() == '' || addrPhoneNum.val() == '' ) {
                        addrMobile.parent().children('span').addClass('required');
                        return;
                    }
                }
                $.ajax({
                    url: '/orders/addresses/new',
                    type: 'POST',
                    dataType: 'json',
                    data: {
                        'address.name': addrName.val(),
                        'address.postcode': addrPost.val(),
                        'address.province': $('#J_addrProv').val(),
                        'address.city': $('#J_addrCity').val(),
                        'address.district': $('#J_addrDist').val(),
                        'address.address': addrStreet.val(),
                        'address.mobile': addrMobile.val(),
                        'address.areaCode': addrAreaCode.val(),
                        'address.phoneNumber': addrPhoneNum.val(),
                        'address.phoneExtNumber': addrPhoneExt.val(),
                        'address.isDefault': true
                    },
                    success: function (data){
                        $('#J_addrCurrent').html( data.fullAddress +' ('+ data.name +' 收) <em>'+ data.mobile +'</em>' );
                        $('#J_modifyAddr').text('[修改]');
                        $('#J_addrAll').hide();

                        $('#J_addrAll ul').prepend('<li class="clearfix" id="addr-li-'+ data.id +'">'
                            +'<span class="addr-one">'
                                +'<input type="radio" name="addr" checked id="addrId_'+ data.id +'" value="'+ data.id +'" />'
                                +'<label for="addrId_'+ data.id +'}"> '+ data.fullAddress +' ('+ data.name +' 收) <em>'+ data.mobile +'</em></label>'
                            +'</span>'
                            +'<span class="addr-action"><a class="addr-edit" data-addrid="'+ data.id +'" href="#">编辑</a> <a class="addr-del" data-addrid="'+ data.id +'" href="#">删除</a></span>'
                        +'</li>');
                    }
                });

            } else if ($(this).attr('data-action') == 'edit-addr') {

                var addrName = $('#J_addrName2'),
                    addrPost = $('#J_addrPost2'),
                    addrStreet = $('#J_addrStreet2'),
                    addrMobile = $('#J_addrMobile2'),
                    addrAreaCode = $('#J_addrAreaCode2'),
                    addrPhoneNum = $('#J_addrPhoneNum2'),
                    addrPhoneExt = $('#J_addrPhoneExt2');
                
                if (addrStreet.val() == '') {
                    addrStreet.parent().append('<span class="required"> 街道地址不能空！</span>');
                    return;
                }
                if (addrPost.val() == '' || !$.isNumeric(addrPost.val()) || !(addrPost.val().length == 6)) {
                    addrPost.parent().append('<span class="required"> 邮政编码格式不对！</span>');
                    return;
                }
                if (addrName.val() == '') {
                    addrName.parent().append('<span class="required"> 收货人不能空！</span>');
                    return;
                }
                if (addrMobile.val() == '' || !$.isNumeric(addrMobile.val()) || !(addrMobile.val().length == 11)) {
                    if (addrAreaCode.val() == '' || addrPhoneNum.val() == '' ) {
                        addrMobile.parent().children('span').addClass('required');
                        return;
                    }
                }
                $.ajax({
                    url: '/orders/addresses/'+ addrId,
                    type: 'PUT',
                    data: {
                        'address.id': addrId,
                        'address.name': $('#J_addrName2').val(),
                        'address.postcode': $('#J_addrPost2').val(),
                        'address.province': $('#J_addrProv2').val(),
                        'address.city': $('#J_addrCity2').val(),
                        'address.district': $('#J_addrDist2').val(),
                        'address.address': $('#J_addrStreet2').val(),
                        'address.mobile': $('#J_addrMobile2').val(),
                        'address.areaCode': $('#J_addrAreaCode2').val(),
                        'address.phoneNumber': $('#J_addrPhoneNum2').val(),
                        'address.phoneExtNumber': $('#J_addrPhoneExt2').val(),
                        'address.isDefault': true
                    },
                    success: function (data){
                        $('#J_addrCurrent').html(data);
                        $('#J_modifyAddr').text('[修改]');
                        $('#J_addrAll').hide();
                        $('#J_addrAll label[for="addrId_'+ addrId +'"]').html(data);
                        $('#J_addrAll input:checked').removeAttr('checked');
                        $('#addrId_'+ addrId).attr('checked', 'checked');
                        $('#J_addrEditBox table').remove();
                        $('#J_confirm').text('送到这个地址').removeAttr('data-action');
                    }
                });
            }
        } else if ($(this).text() == '送到这个地址') {
            $('#addressId').val(addrId);
            $('#J_modifyAddr').text('[修改]');
            $('#J_addrAll').hide();
            var checkedId = $('#J_addrAll input:checked').attr('id'),
                checkHTML = $('#J_addrAll label[for="'+ checkedId +'"]').html();
            $('#J_addrCurrent').html(checkHTML);
        }
    });
});
