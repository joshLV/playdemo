/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-3
 * Time: 下午7:26
 * To change this template use File | Settings | File Templates.
 */


function changeList(status){
    $("#condition_status").val(status);
    $("#frmlist").submit();
}
$(function () {
    $("#selectall").click(function () {
        if (this.checked) {
            $("[name='id']").attr("checked", 'true');//全选
        } else {
            $("[name='id']").removeAttr("checked");//取消
        }
    });

    var checkedcnt = 0;
    $("#deletebtn").click(function () {
        var checkedGoods = [];
        $("input[name='id']").each(function () {
            if (this.checked) {
                checkedcnt++;
                checkedGoods.push($(this).attr("value"));
            }
        });

        if (checkedcnt == 0) {
            alert("请至少选择一条数据！");
        } else {
            if (confirm("您确定要删除吗？")) {
                var url = "/pointgoods/"+checkedGoods.join(",")+"?x-http-method-override=DELETE";
                $("#deletefrm").attr("method","POST") ;
                $("#deletefrm").attr("action",url) ;
                $("#deletefrm").submit();
            }
        }
    });


    /**
     * 同意上架
     */
    $("#onsales").click(function () {
        var checkedGoods = [];
        $("input[name='id']").each(function () {
            if (this.checked) {
                checkedcnt++;
                checkedGoods.push($(this).attr("value"));
            }
        });
        if (checkedcnt == 0) {
            alert("请至少选择一条记录！");
        } else {
            var url = "/pointgoods/"+checkedGoods.join(",")+"/onSale?x-http-method-override=PUT";
            $("#deletefrm").attr("method","POST") ;
            $("#deletefrm").attr("action",url) ;
            $("#deletefrm").submit();
        }
    });

    /**
     * 批量下架
     */
    $("#offsales").click(function () {
        var checkedGoods = [];
        $("input[name='id']").each(function () {
            if (this.checked) {
                checkedcnt++;
                checkedGoods.push($(this).attr("value"));
            }
        });
        if (checkedcnt == 0) {
            alert("请至少选择一条记录！");
        } else {
            var url = "/pointgoods/"+checkedGoods.join(",")+"/offSale?x-http-method-override=PUT";
            $("#deletefrm").attr("method","POST") ;
            $("#deletefrm").attr("action",url) ;
            $("#deletefrm").submit();
        }
    });
    $("#brandId").click(function(){
        $("#frmlist").submit();
    })
});