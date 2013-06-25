package controllers;

import com.taobao.api.ApiException;
import com.taobao.api.Constants;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.LogisticsOfflineSendRequest;
import com.taobao.api.response.LogisticsOfflineSendResponse;
import models.accounts.AccountType;
import models.ktv.KtvTaobaoUtil;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.ExpressCompany;
import models.order.Freight;
import models.order.LogisticImportData;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.order.RealGoodsReturnEntry;
import models.resale.Resaler;
import models.supplier.Supplier;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-13
 * Time: 下午6:15
 */
@With(OperateRbac.class)
@ActiveNavigation("upload_order_shipping_index")
public class UploadOrderShippingInfos extends Controller {
    @ActiveNavigation("upload_order_shipping_index")
    public static void index() {
        List<Supplier> supplierList = Supplier.findSuppliersByCanSaleReal();
        render(supplierList);
    }

    /**
     * 导入发货单
     *
     * @return
     */
    @ActiveNavigation("upload_order_shipping_index")
    public static void upload(File orderShippingFile) {
        List<Supplier> supplierList = Supplier.findSuppliersByCanSaleReal();
        String errorInfo = "";
        if (orderShippingFile == null) {
            errorInfo = "请先选择文件！";
            render("UploadOrderShippingInfos/index.html", errorInfo, supplierList);
        }
        List<LogisticImportData> logistics = new ArrayList<>();
        try {
            //准备转换器
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/UploadOrderShippingInfos/orderShippingTransfer.xml").inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("logistics", logistics);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderShippingFile), beans);
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！";
                Logger.info(errorInfo);
                render("UploadOrderShippingInfos/index.html", errorInfo, supplierList);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！" + e.getMessage();
            e.printStackTrace();
            render("UploadOrderShippingInfos/index.html", errorInfo, supplierList);
        }

        List<String> unExistedOrders = new ArrayList<>();
        List<String> uploadSuccessOrders = new ArrayList<>();
        List<String> unExistedExpressCompanys = new ArrayList<>();
        List<String> emptyExpressInofs = new ArrayList<>();
        List<String> noGoodsCodeList = new ArrayList<>();
        //todo 检查实物订单信息中是否存在退货订单
        List<RealGoodsReturnEntry> returnEntryList = new ArrayList<>();
        for (LogisticImportData logistic : logistics) {
            RealGoodsReturnEntry returnEntry = RealGoodsReturnEntry.findHandling(logistic.orderNumber, logistic.goodsCode);
            if (returnEntry != null && logistic.buyNumber != null && !logistic.buyNumber.equals(returnEntry.orderItems.buyNumber - returnEntry.returnedCount)) {
                returnEntryList.add(returnEntry);
            }
        }
        if (returnEntryList.size() > 0) {
            errorInfo = "上传失败！发货单中有" + returnEntryList.size() + "个退货单，请修改发货单并重新上传，根据列表中<strong>需发货数量</strong>更改发货单中对应订单商品的<strong>数量</strong>。";
            render("UploadOrderShippingInfos/index.html", errorInfo, supplierList, returnEntryList);
        }

        List<LogisticImportData> successTaobaoLogistics = new ArrayList<>();
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);

        for (LogisticImportData logistic : logistics) {
            if (StringUtils.isBlank(logistic.expressCompany)) {
                emptyExpressInofs.add(logistic.orderNumber);
                continue;
            }
            ExpressCompany expressCompany = ExpressCompany.getCompanyNameByCode(logistic.expressCompany);
            if (expressCompany == null) {
                unExistedExpressCompanys.add(logistic.expressCompany);
                continue;
            }
            if (StringUtils.isBlank(logistic.goodsCode)) {
                noGoodsCodeList.add(logistic.goodsCode);
                continue;
            }
            //查询该商户下的订单信息，存在则更新物流信息
            OrderItems orderItems = OrderItems.find("goods.sku is not null and goods.code=? and order.orderNumber=?", logistic.goodsCode, logistic.orderNumber).first();
            if (orderItems == null) {
                unExistedOrders.add(logistic.orderNumber);
                continue;
            }

            orderItems.shippingInfo.expressCompany = expressCompany;
            orderItems.shippingInfo.expressNumber = logistic.expressNumber;
            orderItems.shippingInfo.freight = Freight.findFreight(orderItems.goods.getSupplier(), expressCompany,
                    orderItems.shippingInfo.address);
            orderItems.shippingInfo.save();
            orderItems.status = OrderStatus.SENT;
            orderItems.sendAt = new Date();
            orderItems.save();

            //订单已发货，给商户打钱操作
            orderItems.realGoodsPayCommission();

            uploadSuccessOrders.add(logistic.orderNumber);
            if (resaler != null && orderItems.order.userId.equals(resaler.getId())
                    && orderItems.order.operator.id.equals(resaler.operator.getId())) {
                System.out.println("successTaobaoLogistics = " + successTaobaoLogistics);

                successTaobaoLogistics.add(logistic);
            }
        }

        JPA.em().flush();

        List<ExpressCompany> expressCompanyList = ExpressCompany.findAll();
        renderArgs.put("emptyExpressInofs", emptyExpressInofs);
        renderArgs.put("expressCompanyList", expressCompanyList);
        renderArgs.put("noGoodsCodeList", noGoodsCodeList);
        renderArgs.put("unExistedOrders", unExistedOrders);
        renderArgs.put("unExistedExpressCompanys", unExistedExpressCompanys);
        renderArgs.put("uploadSuccessOrders", uploadSuccessOrders);

        //淘宝自动发货
        List<String> successSendOnTaobao = new ArrayList<>();
        List<String> failSendOnTaobao = new ArrayList<>();

        TaobaoClient taobaoClient = new DefaultTaobaoClient(KtvTaobaoUtil.URL, resaler.taobaoCouponAppKey,
                resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
        //找到淘宝的token
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        for (LogisticImportData logisticImportData : successTaobaoLogistics) {
            LogisticsOfflineSendRequest request = new LogisticsOfflineSendRequest();
            request.setTid(Long.parseLong(logisticImportData.getOuterOrderNo()));
            request.setOutSid(logisticImportData.getExpressNumber());
            request.setCompanyCode(logisticImportData.getExpressCompany().trim());
            try {
                LogisticsOfflineSendResponse response = taobaoClient.execute(request, token.accessToken);
                if (response.isSuccess()) {
                    successSendOnTaobao.add(logisticImportData.getOuterOrderNo());
                } else {
                    Logger.error("淘宝确认收货失败 %s %s", response.getSubCode(), response.getSubMsg());
                    failSendOnTaobao.add(logisticImportData.getOuterOrderNo());
                }
            } catch (ApiException e) {
                Logger.error(e, "淘宝确认收货失败");
            }
        }
        renderArgs.put("successSendOnTaobao", successSendOnTaobao);
        renderArgs.put("failSendOnTaobao", failSendOnTaobao);


        render("UploadOrderShippingInfos/index.html", supplierList);
    }


}
