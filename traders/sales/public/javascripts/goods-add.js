/**
 * To Goods/add.html.
 *
 * User: sujie
 * Date: 3/9/12
 * Time: 7:00 PM
 */
KE.show({
    id:'prompt',
    imageUploadJson:'@{UploadFile.uploadJson()}',
    fileManagerJson:'@{UploadFile.fileManagerJson()}',
    allowFileManager:true,
    afterCreate:function (id) {
        KE.event.ctrl(document, 13, function () {
            KE.util.setData(id);
            document.forms['frm'].submit();
        });
        KE.event.ctrl(KE.g[id].iframeDoc, 13, function () {
            KE.util.setData(id);
            document.forms['frm'].submit();
        });
    }
});

KE.show({
    id:'details',
    imageUploadJson:'@{UploadFile.uploadJson()}',
    fileManagerJson:'@{UploadFile.fileManagerJson()}',
    allowFileManager:true,
    afterCreate:function (id) {
        KE.event.ctrl(document, 13, function () {
            KE.util.setData(id);
            document.forms['frm'].submit();
        });
        KE.event.ctrl(KE.g[id].iframeDoc, 13, function () {
            KE.util.setData(id);
            document.forms['frm'].submit();
        });
    }
});

$(window).load(
    function () {

        $("#onsales").click(function () {
            $("#status").val("ONSALE");
        });
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
    }
);
