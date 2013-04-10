package controllers.real;

import cache.CacheCallBack;
import cache.CacheHelper;
import controllers.OperateRbac;
import models.accounts.AccountType;
import models.order.*;
import models.resale.Resaler;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.modules.paginate.ValuePaginator;
import play.mvc.After;
import play.mvc.Controller;
import play.mvc.With;
import util.DateHelper;

import java.util.*;

/**
 * 下载带运单号的跟踪表文件，供上传到不同.
 * User: tangl
 * Date: 13-3-12
 * Time: 下午5:54
 */
@With(OperateRbac.class)
@ActiveNavigation("download_track_no_for_resaler")
public class DownloadTrackNos extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 选择一个渠道，然后出现下载列表.
     *
     * @param
     */
    public static void index(DownloadTrackNoCondition condition) {

        int pageNumber = getPage();
        if (condition == null) {
            condition = new DownloadTrackNoCondition();
            condition.sentBeginAt = DateHelper.beforeDays(3);
            condition.sentEndAt = new Date();
        }
        List<DownloadTrackNoReport> resultList = DownloadTrackNoReport.query(condition);

//        分页
        ValuePaginator<DownloadTrackNoReport> shippingList = utils.PaginateUtil.wrapValuePaginator(resultList, pageNumber, PAGE_SIZE);
        render(shippingList, pageNumber, condition);
    }


    private static int getPage() {
        String page = request.params.get("page");
        if (StringUtils.isNotEmpty(page) && (page.contains("?x-http-method-override=PUT") || page.contains("x-http-method-override=PUT"))) {
            page = page.replace("x-http-method-override=PUT", "").replace("?", "");
        }
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        return pageNumber;
    }

    private static void jdShippingExcelOut(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, Boolean unDownloaded, String outerGoodsNo) {
        List<OrderItems> orderItemsList = DownloadTrackNoReport.queryOrderItems(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "京东发货单导出_" + System.currentTimeMillis() + ".xls");
        Map<String, String> channelExpressMap = models.order.ExpressCompany.findChannelExpress();

        //更新orderItems的状态为：已上传
        for (OrderItems item : orderItemsList) {
            item.status = OrderStatus.UPLOADED;
            OuterOrder outerOrder = OuterOrder.getOuterOrder(item.order);
            item.outerOrderId = outerOrder.orderId;
            if (channelExpressMap.size() == 0) {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.id + "快递编码");
            } else if (StringUtils.isNotBlank(channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id))) {
                item.shippingInfo.channelExpressNo = channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id);
            } else {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.name + "快递编码");
            }
        }
        for (OrderItems item : orderItemsList) {
            item.save();
        }
        render("real/DownloadTrackNos/jdShippingExcelOut.xls", orderItemsList);
    }

    public static void yhdShippingExcelOut(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, Boolean unDownloaded, String outerGoodsNo) {
        List<OrderItems> orderItemsList = DownloadTrackNoReport.queryOrderItems(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "一号店发货单导出_" + System.currentTimeMillis() + ".xls");
        Map<String, String> channelExpressMap = models.order.ExpressCompany.findChannelExpress();

        //更新orderItems的状态为：已上传
        for (OrderItems item : orderItemsList) {


            item.status = OrderStatus.UPLOADED;
            OuterOrder outerOrder = OuterOrder.getOuterOrder(item.order);
            item.outerOrderId = outerOrder.orderId;
            if (channelExpressMap.size() == 0) {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.id + "快递编码");
            } else if (StringUtils.isNotBlank(channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id))) {
                item.shippingInfo.channelExpressNo = channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id);
            } else {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.name + "快递编码");
            }
        }

        for (OrderItems item : orderItemsList) {
            item.save();
        }
        render("real/DownloadTrackNos/yhdShippingExcelOut.xls", orderItemsList);
    }


    public static void wbShippingExcelOut(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, Boolean unDownloaded, String outerGoodsNo) {
        List<OrderItems> orderItemsList = DownloadTrackNoReport.queryOrderItems(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "58发货单导出_" + System.currentTimeMillis() + ".xls");
        Map<String, String> channelExpressMap = models.order.ExpressCompany.findChannelExpress();

        //更新orderItems的状态为：已上传
        for (OrderItems item : orderItemsList) {
            item.status = OrderStatus.UPLOADED;
            OuterOrder outerOrder = OuterOrder.getOuterOrder(item.order);
            item.outerOrderId = outerOrder.orderId;
            if (channelExpressMap.size() == 0) {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.id + "快递编码");
            } else if (StringUtils.isNotBlank(channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id))) {
                item.shippingInfo.channelExpressNo = channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id);
            } else {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.name + "快递编码");
            }
        }

        for (OrderItems item : orderItemsList) {
            item.save();
        }
        render("real/DownloadTrackNos/wbShippingExcelOut.xls", orderItemsList);
    }

    public static void tbShippingExcelOut(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, Boolean unDownloaded, String outerGoodsNo) {
        List<OrderItems> orderItemsList = DownloadTrackNoReport.queryOrderItems(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
        request.format = "xls";
        renderArgs.put("__FILE_NAME__", "淘宝发货单导出_" + System.currentTimeMillis() + ".xls");
        Map<String, String> channelExpressMap = models.order.ExpressCompany.findChannelExpress();

        //更新orderItems的状态为：已上传
        for (OrderItems item : orderItemsList) {
            item.status = OrderStatus.UPLOADED;
            OuterOrder outerOrder = OuterOrder.getOuterOrder(item.order);
            item.outerOrderId = outerOrder.orderId;
            if (channelExpressMap.size() == 0) {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.id + "快递编码");
            } else if (StringUtils.isNotBlank(channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id))) {
                item.shippingInfo.channelExpressNo = channelExpressMap.get(partner.toString() + "-" + item.shippingInfo.expressCompany.id);
            } else {
                errors(partner.partnerName() + "没有对应的" + item.shippingInfo.expressCompany.name + "快递编码");
            }
        }

        for (OrderItems item : orderItemsList) {
            item.save();
        }
        render("real/DownloadTrackNos/tbShippingExcelOut.xls", orderItemsList);
    }


    /**
     * 通过下载链接下载对应的excel文件.
     *
     * @param partner
     * @param paidBeginAt
     * @param paidEndAt
     * @param sentBeginAt
     * @param sentEndAt
     * @param unDownloaded
     * @param outerGoodsNo
     */
    public static void download(OuterOrderPartner partner, Date paidBeginAt, Date paidEndAt, Date sentBeginAt, Date sentEndAt, Boolean unDownloaded, String outerGoodsNo) {
        switch (partner) {
            case JD:
                jdShippingExcelOut(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
                break;
            case YHD:
                yhdShippingExcelOut(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
                break;
            case WB:
                wbShippingExcelOut(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
                break;
            case TB:
                tbShippingExcelOut(partner, paidBeginAt, paidEndAt, sentBeginAt, sentBeginAt, unDownloaded, outerGoodsNo);
                break;
            default:
                renderText("不支持类型");
        }
    }

    public static void errors(String errorInfo) {
        renderText(errorInfo);
    }

    @After
    public static void clearCache() {
        CacheHelper.cleanPreRead();
    }

}