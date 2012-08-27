/**
 * For SupplierGoods/index.html.
 * User: sujie
 * Date: 3/15/12
 * Time: 5:41 PM
 */
//
//function deleteSupplier(link) {
//    if (confirm("您确定要删除商品[" + link.name + "]吗？")) {
//        var url = "/goods/" + link.id;
//        $.ajax({
//            url:url,
//            type:'DELETE',
//            error:function () {
//                alert('删除失败!');
//            },
//            success:function (msg) {
//                window.location.reload();
//            }
//        });
//    }
//}
//
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
                var url = "/goods/"+checkedGoods.join(",")+"?x-http-method-override=DELETE";
                $("#deletefrm").attr("method","POST") ;
                $("#deletefrm").attr("action",url) ;
                $("#deletefrm").submit();
            }
        }
    });

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
            var url = "/goods/"+checkedGoods.join(",")+"/apply?x-http-method-override=PUT";
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
            var url = "/goods/"+checkedGoods.join(",")+"/offSale?x-http-method-override=PUT";
            $("#deletefrm").attr("method","POST") ;
            $("#deletefrm").attr("action",url) ;
            $("#deletefrm").submit();
        }
    });

});