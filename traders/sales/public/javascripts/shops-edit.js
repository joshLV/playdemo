/**
 * For sales/Shops/edit.html.
 * User: sujie
 * Date: 3/15/12
 * Time: 11:59 AM
 */
$(document).ready(function () {
    $("#district").change(function () {
        loadArea($("#district").val());
    });

    function loadArea(areaId) {
        var url = "@{Shops.relation()}";
        $.ajax({
            url:url,
            data:"areaId=" + areaId,
            type:'GET',
            dataType:'JSON',
            error:function () {
                alert('Error loading data!');
            },
            success:function (msg) {
                $("#area").empty();
                $.each(eval(msg), function (i, item) {
                    $("<option value='" + item.id + "'>" + item.name + "</option>").appendTo($("#area"));
                });
            }
        });
        $("#area").change();
    }

    $("#cancel").click(function () {
        window.open('/shops', '_self');
    });
});

