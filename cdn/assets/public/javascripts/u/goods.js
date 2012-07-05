/**
 * Created with IntelliJ IDEA.
 * User: yanjy
 * Date: 12-7-4
 * Time: 下午2:45
 * To change this template use File | Settings | File Templates.
 */
function support(id, type) {
    $.post(
        "/goods/statistics ",
        {'id':id, 'statisticsType':type},
        function (data) {
            $('#summary_' + id).html(data);
        }
    );
}