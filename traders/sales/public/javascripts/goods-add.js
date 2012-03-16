/**
 * To Goods/add.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */
//KE.show({
//    id:'prompt',
//    imageUploadJson:'@{UploadFiles.uploadJson()}',
//    fileManagerJson:'@{UploadFiles.fileManagerJson()}',
//    allowFileManager:true,
//    afterCreate:function (id) {
//        KE.event.ctrl(document, 13, function () {
//            KE.util.setData(id);
//            document.forms['frm'].submit();
//        });
//        KE.event.ctrl(KE.g[id].iframeDoc, 13, function () {
//            KE.util.setData(id);
//            document.forms['frm'].submit();
//        });
//    }
//});
//
//KE.show({
//    id:'details',
//    imageUploadJson:'@{UploadFiles.uploadJson()}',
//    fileManagerJson:'@{UploadFiles.fileManagerJson()}',
//    allowFileManager:true,
//    afterCreate:function (id) {
//        KE.event.ctrl(document, 13, function () {
//            KE.util.setData(id);
//            document.forms['frm'].submit();
//        });
//        KE.event.ctrl(KE.g[id].iframeDoc, 13, function () {
//            KE.util.setData(id);
//            document.forms['frm'].submit();
//        });
//    }
//});

$(function () {
    var editorPrompt = KindEditor.create('textarea[name="goods.prompt"]',
        {
            filterMode:true,
            uploadJson:'/goods/images',
            allowFileManager:false
        }
    );
    var editorDetails = KindEditor.create('textarea[name="goods.details"]',
        {
            filterMode:true,
            uploadJson:'/goods/images',
            allowFileManager:false
        }
    );

});


$(window).load(
    function () {
        $("#topCategoryId").change(function () {
            $("#categoryId").load("/category/sub/" + $("#topCategoryId").val(), function (data) {
                var categoryList = $.parseJSON(data);
                $("#categoryId").empty();
                $.each(categoryList, function (i, category) {
                    $("#categoryId").append("<option value='" + category.id + "'>" + category.name + "</option>");
                });
            });
        });

        $("#onsales").click(function () {
                if ($("#baseSale").val() > 0) {
                    $("#status").val("ONSALE");
                } else {
                    $("#errorBaseSale").text("上架商品的库存不能为0！");
                    return false;
                }
            }
        )
        ;
        $("#isAllShop2").click(function () {
            $("#shop").show();//显示门店列表
        });
        $("#isAllShop1").click(function () {
            $("#shop").hide();//隐藏门店列表
        });
        $("#view").click(function () {
            $("#status").val("OFFSALE");
        });
        $("#save").click(function () {
            $("#status").val("OFFSALE");
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
        });
    }
);
