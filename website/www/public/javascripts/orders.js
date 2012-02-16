/**
 * 添加地址行显示到地址列表中
 * @param data
 */
function addAddressToList(data) {
    var listAddressUl = $('#list_address_ul');

    listAddressUl.append($("<li class='li_address_modify' id='li_address_'" + data.id + "' addressId='" + data.id + "'><input name=\"selectedAddressId\" id=\"radio_address_" + data.id + "\" type='radio' value='" + data.id + "' checked/>" +
        "<span class=\"span_name\" id=\"address_name_" + data.id + "\">" + data.name + "</span>" +
        "<span class=\"span_address\" id=\"address_address_" + data.id + ">" + data.address + "</span>" +
        "<span class=\"span_tel\" id=\"address_postcode_" + data.id + ">" + data.postcode + "</span>" +
        "<span class=\"span_tel\" id=\"address_mobile_" + data.id + ">" + data.mobile + "</span>" +
        "<span class=\"span_tel\" id=\"address_phone_" + data.id + ">" + data.phone + "</span>" +
        "<span class=\"mod_span_d\"><a class=\"address_span_modify\" href='#'>修改</a>|<a class=\"address_span_delete\" href='#'>删除</a></span></li>"));
}

function setAddress(addressId) {
    if (addressId == 0) {//如果是新增的地址
        $("#show_name").html($("#address_name").val());
        $("#show_address").html($("#address_province").val() + " " + $("#address_city").val() + " " + $("#address_district").val() + " " + $("#address_address").val());
        $("#show_postcode").html($("#address_postcode").val());
        alert("areaCode=" + $("#address_areaCode").val());
        alert("address_phoneNumber=" + $("#address_phoneNumber").val());
        alert("address_phoneExtNumber=" + $("#address_phoneExtNumber").val());
        var areaCode = $("#address_areaCode").val() == "" ? "" : $("#address_areaCode").val() + "-";
        var phone = $("#address_mobile").val() + "   " + areaCode + $("#address_phoneNumber").val();
        if ($("#address_phoneExtNumber").val() != "") {
            phone += "-" + $("#address_phoneExtNumber").val();
        }
        $("#show_phone").html(phone);
    } else {//数据库中原有的地址
        alert($("#address_name_" + addressId).val());
        $("#show_name").html($("#address_name_" + addressId).val());
//            $("#show_address").html($("#address_province_" + addressId).val() + " " + $("#address_city_" + addressId).val() + " " + $("#address_district_" + addressId).val() + $("#address_address_" + addressId).val());
        $("#show_address").html($("#address_address_" + addressId).val());
        $("#show_postcode").html($("#address_postcode_" + addressId).val());
        $("#show_phone").html($("#address_mobile_" + addressId).val() + " " + $("#address_phone_" + addressId).val());
//            var areaCode = $("#address_areaCode_" + addressId).val() == null ? "" : $("#address_areaCode_" + addressId).val() + "-";
//            var phone = $("#address_mobile_" + addressId).val() + "   " + areaCode + $("#address_phoneNumber_" + addressId).val();
//            if ($("#address_phoneExtNumber_" + addressId).val() != null) {
//                phone += "-" + $("#address_phoneExtNumber_" + addressId).val();
//            }
//            $("#show_phone").html(phone);

    }
}

function showAddress() {
    $("#list_address_ul").css("display", "none");
    $("#edit_address_ul").css("display", "none");
    $("#show_address_ul").css("display", "");
}


function showCreateAddressEdit() {
    $("#radio_address_0").attr("checked", true);
    if ($("#div_edit_address").css("display") == "none") {
        $("#div_edit_address").css("display", "");
    } else {
        $("#div_edit_address").css("display", "none");
    }
}
$(window).load(
    function () {
        //地址表格的删除按钮事件
        $(".address_span_delete").click(function () {
            if (confirm("您确定要删除这个地址吗?")) {
                addressId = $(this).attr('addressId');
                $.ajax({
                    type:'DELETE',
                    url:'/orders/addresses/' + addressId,
                    success:function (data) {
                        //todo 判断删除结果
                        $("#li_address_" + addressId).remove();
                    }});
            }
        });
        //地址表格的修改按钮事件
        $(".address_span_modify").click(function () {
            addressId = $(this).attr('addressId');

        });

        $(".li_address_modify").click(function () {
            addressId = $(this).attr('addressId');
            $("#radio_address_" + addressId).attr("checked", true);
            showCreateAddressEdit();
        });

        /**
         * 使用新地址层的显示和隐藏事件
         */
        $("#radio_address_0").click(function () {
            showCreateAddressEdit();
        });

        /**
         * 使用新地址层的显示和隐藏事件
         */
        $("#span_use_new_address").click(function () {
            showCreateAddressEdit();
        });

        /**
         * 点击确认收货信息按钮.
         */
        $("#link_confirm").click(function () {
            //添加收货地址信息到数据库中
            var addressId = $("input[name='selectedAddressId']:checked").val();
            var addNew = addressId == 0;
            if (addNew) {
//                var addAction = #{jsAction @Addresses.create()/}
                $.post("/orders/addresses",
                    {selectedAddressId:$("#selectedAddressId").val(),
                        'address.name':$("#address_name").val(),
                        'address.postcode':$("#address_postcode").val(),
                        'address.province':$("#address_province").val(),
                        'address.city':$("#address_city").val(),
                        'address.district':$("#address_district").val(),
                        'address.address':$("#address_address").val(),
                        'address.mobile':$("#address_mobile").val(),
                        'address.areaCode':$("#address_areacode").val(),
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

            }
            //设置当前用户的收货地址信息
            setAddress(addressId);
            showAddress();
            showCreateAddressEdit();

        });

        $("#link_edit_show_address").click(function () {
            $("#list_address_ul").css("display", "");
            $("#edit_address_ul").css("display", "");
            $("#show_address_ul").css("display", "none");
            $("#address_province").focus();
        });


        $("#link_mobile_confirm").click(function () {
            $("#ecart_mobile_show_span").html($("#ecart_mobile").val());
            $("#ecart_mobile_show_dd").css("display", "");
            $("#ecart_mobile_update_dd").css("display", "none");
        });

        $("#link_edit_mobile").click(function () {
            $("#ecart_mobile").val($("#ecart_mobile_show_span").html());

            $("#ecart_mobile_show_dd").css("display", "none");
            $("#ecart_mobile_update_dd").css("display", "");
        });
    }
);