/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-3
 * Time: 上午11:23
 * To change this template use File | Settings | File Templates.
 */


$(
    function () {


        $("#onsales").click(function () {
            if ($("#baseSale").val() > 0) {
                $("#status").val("ONSALE");
                $("#form").attr("target", "_self");
            } else {
                $("#errorBaseSale").text("上架商品的库存不能为0！");
                $("#form").attr("target", "_self");
                return false;
            }
        });

        $("#save").click(function () {
            $("#status").val("OFFSALE");
            $("#form").attr("target", "_self");
        });
        $("#onsale").click(function () {
            $("#status").val("ONSALE");
            $("#form").attr("target", "_self");
        });
        $("#preview").click(function () {
            $("#status").val("UNCREATED");
            $("#form").attr("target", "_blank");
        });


//        $("#imagePath").click(function () {
//            var imagePath = $("#imagePath").val();
//            if (imagePath != '') {
//                $.ajax({
//                    url:'/pointgoods/imageName',
//                    data:'imagePath=' +imagePath,
//                    type:'GET',
//                    error:function () {
//                        alert('取得失败!');
//                    },
//                    success:function (msg) {
//                        $("#info").html(msg);
//                    }
//                });
//            }
//
//        });
        $("#imagePath").change(function(){
            var fileName =  $(this).val().split(/\\/).pop();
            var fileNameFull =  $("#imagePath").val();

            if (fileName != undefined && fileName != ""){
                $("#info").html(fileName);
//                $("#editTime").html("修改后");
//                alert(fileNameFull)
//                  $("#imgPath").attr("src",fileNameFull);
//                $("#imageLargePath").val(fileNameFull);
            }
        });
    }
);
