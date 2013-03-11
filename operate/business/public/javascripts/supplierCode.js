/**
 * User: wangjia
 * Date: 12-11-30
 * Time: 下午2:12
 * To change this template use File | Settings | File Templates.
 */
$(
    function () {
        $("#supplier_supplierCategory_id").change(function () {
            $("#supplier_code").load("/suppliers/" + $("#id").val()
                + "/supplier_category/" + $("#supplier_supplierCategory_id").val(), function (data) {
            });

        });
    })

