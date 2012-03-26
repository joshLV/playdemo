/**
 * For SupplierGoods/index.html.
 * User: sujie
 * Date: 3/15/12
 * Time: 5:41 PM
 */
$(function () {
    $("#selectall").click(function () {
        if (this.checked) {
            $("[name='ids[]']").attr("checked", 'true');//全选
        } else {
            $("[name='ids[]']").removeAttr("checked");//取消
        }
    });

    var checkedcnt = 0;
    $("#deletebtn").click(function () {
        $("input[name='ids[]']").each(function () {
            if (this.checked) {
                checkedcnt++;
            }
        });
        if (checkedcnt == 0) {
            alert("请至少选择一条数据！");
        } else {
            if (confirm("您确定要删除吗？")) {
                $("#deletefrm").attr("method", "delete");
                $("#deletefrm").attr("action", "@{SupplierGoods.delete()}");
                $("#deletefrm").submit();
            }
        }
    });

    $("#onsales").click(function () {
        $("input[name='ids[]']").each(function () {
            if (this.checked) {
                checkedcnt++;
            }
        });
        if (checkedcnt == 0) {
            alert("请至少选择一条记录！");
        } else {
            $("#deletefrm").attr("method", "POST");
            $("#status").val("ONSALE");
            $("#deletefrm").action = "@{SupplierGoods.updateStatus()}";
            $("#deletefrm").submit();
        }
    });

    $("#offsales").click(function () {
        $("input[name='ids[]']").each(function () {
            if (this.checked) {
                checkedcnt++;
            }
        });
        if (checkedcnt == 0) {
            alert("请至少选择一条记录！");
        } else {
            $("#status").val("OFFSALE");
            $("#deletefrm").attr("method", "POST");
            $("#deletefrm").action = "@{SupplierGoods.updateStatus()}";
            $("#deletefrm").submit();
        }
    });

});