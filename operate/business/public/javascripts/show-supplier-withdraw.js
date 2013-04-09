$(function () {

//门店列表跟随商户名联动的方法
    $("#supplierName").blur(function () {
        var found = false;
        var m = list.length;
        var id = null;
        for (i = 0; i < m; i++) {
            if (list[i].name == $("#supplierName").val()) {
                found = true;
                id = list[i].id;
            }
        }
        if (!found) {
            $("#id_supplierName").val(-1);
            $("#save").attr('disabled', true);
            $("#shopId").add(new Option("", "不限"));
        } else {
            console.log("test!!!!!!!!!!!!");
            $("#shop").load("/shops/" + id + "/showIndependentShops", function (data) {
            });
        }
//        $("#supplierUser_shop_id").options[0].selected = true;

        if ($("#supplierName").val().trim() == '') {
            $("#id_supplierName").val(0);
            $("#supplierUser_shop_id").empty();
        }
    });
});
