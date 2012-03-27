/**
 * For Suppliers/index.html.
 * User: sujie
 * Date: 3/21/12
 * Time: 2:27 PM
 */

function deleteSupplier(link) {
    if (confirm("您确定要删除商户[" + link.name + "]吗？")) {
        var url = "/suppliers/" + link.id;
        $.ajax({
            url:url,
            type:'DELETE',
            error:function () {
                alert('删除失败!');
            },
            success:function (msg) {
                window.location.reload();
            }
        });
    }
}