package controllers;

import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.Constants;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Trade;
import com.taobao.api.request.TradeGetRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.response.TradeGetResponse;
import com.taobao.api.response.TradesSoldGetResponse;
import models.accounts.Account;
import models.accounts.AccountType;
import models.accounts.PaymentSource;
import models.ktv.KtvTaobaoUtil;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.*;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-5
 * Time: 下午2:21
 */
@With(OperateRbac.class)
@ActiveNavigation("import_order_index")
public class ImportPartnerOrders extends Controller {
    @ActiveNavigation("import_order_index")
    public static void index() {
        render();
    }

    /**
     * 下载各渠道的订单模板
     *
     * @param partner
     */
    public static void download(OuterOrderPartner partner) {
        renderBinary(VirtualFile.fromRelativePath("app/views/ImportPartnerOrders/" + partner.partnerName() + "_订单模板.xls").getRealFile());
    }

    /**
     * 导入渠道订单
     *
     * @param orderFile
     * @param partner
     */
    public static void upload(File orderFile, OuterOrderPartner partner) {
        String errorInfo = "";
        if (orderFile == null) {
            errorInfo = "请先选择文件！";
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }
        List<LogisticImportData> logistics = new ArrayList<>();
        try {
            //准备转换器
            Logger.info("partner==" + partner);
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/ImportPartnerOrders/" + partner.toString().toLowerCase() + "Transfer.xml")
                    .inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("logistics", logistics);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderFile), beans);
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！";
                Logger.info(errorInfo);
                render("ImportPartnerOrders/index.html", errorInfo, partner);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！" + e.getMessage();
            e.printStackTrace();
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }
        List<String> existedOrderList = new ArrayList<>();
        List<String> notEnoughInventoryGoodsList = new ArrayList<>();
        List<String> importSuccessOrderList = new ArrayList<>();
        Set<String> unBindGoodsSet = new HashSet<>();
        List<String> diffOrderPriceList = new ArrayList<>();
        processLogisticList(logistics, partner, existedOrderList, notEnoughInventoryGoodsList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList);
        render("ImportPartnerOrders/index.html", partner, errorInfo, existedOrderList, notEnoughInventoryGoodsList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList);
    }


    public static void autoCreateTaobaoRealOrder() {
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);

        TaobaoClient taobaoClient = new DefaultTaobaoClient(KtvTaobaoUtil.URL, resaler.taobaoCouponAppKey,
                resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
        //找到淘宝的token
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        TradesSoldGetRequest request = new TradesSoldGetRequest();
        request.setFields("orders.outer_iid,tid,orders.payment,orders.num,pay_time,orders.price,shipping_type," +
                "orders.logistics_company,receiver_mobile,receiver_name,receiver_state,receiver_city," +
                "receiver_district,receiver_address,receiver_zip,has_buyer_message");
        request.setStatus("WAIT_SELLER_SEND_GOODS");
        long page = 1L;
        long pageSize = 100L;
        request.setPageNo(page);
        request.setUseHasNext(true);
        request.setPageSize(pageSize);

        String errorInfo = "";

        List<LogisticImportData> logisticImportDataList = new ArrayList<>();
        while (true) {
            TradesSoldGetResponse  response;
            try {
                response = taobaoClient.execute(request, token.accessToken);
                if (!response.isSuccess()) {
                    errorInfo = response.getSubCode();
                    break;
                }else {
                    List<Trade> tradeList = response.getTrades();
                    for (Trade trade : tradeList) {
                        List<com.taobao.api.domain.Order> orderList = trade.getOrders();
                        String buyerMessage = "";
                        if (trade.getHasBuyerMessage()) {
                            //如果买家有留言，就去单独获取留言
                            TradeGetRequest tradeGetRequest = new TradeGetRequest();
                            tradeGetRequest.setFields("buyer_message");
                            tradeGetRequest.setTid(trade.getTid());
                            try {
                                TradeGetResponse tradeGetResponse = taobaoClient.execute(tradeGetRequest, token.accessToken);
                                buyerMessage = tradeGetResponse.getTrade().getBuyerMessage();
                            } catch (ApiException e) {
                                throw new RuntimeException("request taobao trade info error", e);
                            }
                        }
                        for (com.taobao.api.domain.Order order : orderList) {
                            if (trade.getShippingType().equals("virtual")) {
                                Logger.info("虚拟发货 跳过: " + trade.getTid());
                                continue;
                            }
                            LogisticImportData logistic = new LogisticImportData();
                            logistic.setOuterOrderNo(String.valueOf(trade.getTid()));
                            logistic.setOuterGoodsNo(String.valueOf(order.getOuterIid()));
                            BigDecimal salePrice = new BigDecimal(order.getPayment()).divide(new BigDecimal(order.getNum()), RoundingMode.DOWN);
                            logistic.setSalePrice(salePrice);
                            logistic.setBuyNumber(order.getNum());
                            logistic.setPaidAt(trade.getPayTime());
                            logistic.setExpressInfo(order.getLogisticsCompany());
                            logistic.setPhone(trade.getReceiverMobile());
                            logistic.setReceiver(trade.getReceiverName());
                            logistic.setZipCode(trade.getReceiverZip());
                            logistic.setRemarks(buyerMessage);
                            logistic.setAddress(trade.getReceiverState()+trade.getReceiverCity()+trade.getReceiverDistrict()+trade.getReceiverAddress());
                            logisticImportDataList.add(logistic);
                        }
                    }
                }
            }catch (ApiException e) {
                errorInfo = e.getErrMsg();
                break ;
            }
            if (!response.getHasNext()) {
                break;
            }
            page += 1;
        }
        List<String> existedOrderList = new ArrayList<>();
        List<String> notEnoughInventoryGoodsList = new ArrayList<>();
        List<String> importSuccessOrderList = new ArrayList<>();
        Set<String> unBindGoodsSet = new HashSet<>();
        List<String> diffOrderPriceList = new ArrayList<>();
        System.out.println(new Gson().toJson(logisticImportDataList));
        processLogisticList(logisticImportDataList, OuterOrderPartner.TB, existedOrderList,
                notEnoughInventoryGoodsList, importSuccessOrderList, unBindGoodsSet, diffOrderPriceList);

        OuterOrderPartner partner = OuterOrderPartner.TB;
        render("ImportPartnerOrders/index.html", partner, errorInfo, existedOrderList, notEnoughInventoryGoodsList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList);
    }

    private static void processLogisticList(List<LogisticImportData> logistics, OuterOrderPartner partner,
            List<String> existedOrderList, List<String> notEnoughInventoryGoodsList,
            List<String> importSuccessOrderList, Set<String> unBindGoodsSet, List<String> diffOrderPriceList) {
        for (LogisticImportData logistic : logistics) {
            Logger.info("Process OrderNO: %s", logistic.outerOrderNo);

            OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, logistic.outerOrderNo).first();

            if (outerOrder != null) {
                if (outerOrder.orderType != OuterOrderType.IMPORT && outerOrder.status == OuterOrderStatus.ORDER_IGNORE) {
                    //如果 状态不是IMPORT， 并且status 是IGNORE，说明可能是一号店API拉去过来的订单，我们这里给转成导入的订单
                    outerOrder.message = new Gson().toJson(logistic);
                    outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                    outerOrder.orderType = OuterOrderType.IMPORT;
                } else {
                    existedOrderList.add(logistic.outerOrderNo);
                    continue;
                }
            } else {
                outerOrder = logistic.toOuterOrder(partner);
            }
            if (partner == OuterOrderPartner.WB) {
                createWubaYbqOrder(logistic, partner, outerOrder, importSuccessOrderList, unBindGoodsSet,
                        notEnoughInventoryGoodsList, diffOrderPriceList);
            } else {
                createYbqOrder(logistic, outerOrder, partner, notEnoughInventoryGoodsList, importSuccessOrderList, unBindGoodsSet);
            }
        }

    }
    private static void createWubaYbqOrder(LogisticImportData logistic, OuterOrderPartner partner, OuterOrder outerOrder,
            List<String> importSuccessOrderList, Set<String> unBindGoodsSet, List<String> notEnoughInventoryGoodsList,
            List<String> diffOrderPriceList) {
        //先取得订单总金额
        BigDecimal orderAmount = logistic.salePrice;
        Resaler wubaResaler = Resaler.findApprovedByLoginName(Resaler.WUBA_LOGIN_NAME);
        List<LogisticImportData> wubaLogistics = logistic.processWubaLogistic();
        Order ybqOrder = logistic.createYbqOrderByWB(partner);
        //save ybqOrder info
        if (ybqOrder != null) {
            outerOrder.ybqOrder = ybqOrder;
            outerOrder.save();
            ybqOrder.paidAt = logistic.paidAt;
            ybqOrder.createdAt = logistic.paidAt;
            ybqOrder.save();
            importSuccessOrderList.add(logistic.outerOrderNo);
        }

        OrderShippingInfo orderShipInfo = logistic.createOrderShipInfo();
        BigDecimal nowOrderAmount = BigDecimal.ZERO;
        for (LogisticImportData wubaGoodsInfo : wubaLogistics) {
            Goods goods = ResalerProduct.getGoodsByPartnerProductId(wubaResaler, wubaGoodsInfo.outerGoodsNo,
                    partner);
            if (goods == null) {
                //未映射商品
                Logger.info("未映射商品NO=" + logistic.outerGoodsNo + " NOT Found!");
                unBindGoodsSet.add(logistic.outerGoodsNo);
                outerOrder.delete();
                continue;
            }
            try {
                nowOrderAmount = nowOrderAmount.add(goods.salePrice.multiply(new BigDecimal(wubaGoodsInfo.buyNumber)));
                //产生orderItem
                logistic.createOrderItem(ybqOrder, goods, orderShipInfo, wubaGoodsInfo.buyNumber, goods.salePrice);
            } catch (NotEnoughInventoryException e) {
                e.printStackTrace();
                notEnoughInventoryGoodsList.add(logistic.outerGoodsNo);
                importSuccessOrderList.clear();
            }
        }

        //订单价格和商品单价*数量不一致的场合
        if (orderAmount.compareTo(nowOrderAmount) != 0) {
            diffOrderPriceList.add(logistic.outerOrderNo);
            ybqOrder.rebateValue = orderAmount.subtract(nowOrderAmount);
        }

        ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        Account account = ybqOrder.chargeAccount();
        ybqOrder.paid(account);
        ybqOrder.paidAt = logistic.paidAt;
        ybqOrder.save();
    }

    /**
     * 除了WUBA以外的订单导入
     */
    private static void createYbqOrder(LogisticImportData logistic, OuterOrder outerOrder, OuterOrderPartner partner,
                                       List<String> notEnoughInventoryGoodsList, List<String> importSuccessOrderList, Set<String> unBindGoodsSet) {
        Order ybqOrder;
        try {
            ybqOrder = logistic.toYbqOrder(partner);
            //save ybqOrder info
            if (ybqOrder != null) {
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
                ybqOrder.paidAt = logistic.paidAt;
                ybqOrder.createdAt = logistic.paidAt;
                ybqOrder.save();
                importSuccessOrderList.add(logistic.outerOrderNo);
            } else {
                //未映射商品
                Logger.info("未映射商品NO=" + logistic.outerGoodsNo + " NOT Found!");
                unBindGoodsSet.add(logistic.outerGoodsNo);
            }
        } catch (NotEnoughInventoryException e) {
            e.printStackTrace();
            notEnoughInventoryGoodsList.add(logistic.outerGoodsNo);
            JPA.em().getTransaction().rollback();
            importSuccessOrderList.clear();
        }
    }

}
