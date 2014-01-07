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
import org.apache.commons.lang.StringUtils;
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
import java.util.*;

/**
 * <p/>
 * User: yanjy
 * Date: 13-3-5
 * Time: 下午2:21
 */
@With(OperateRbac.class)
@ActiveNavigation("import_order_index")
public class ImportPartnerOrders extends Controller {
    public static Boolean TB_AUTO_IMPORT_REAL_ORDER = true;
    public static Boolean MANUAL_IMPORT_REAL_ORDER = false;

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
        //取得京东的文件名,文件名即为京东的商品编号
        String fileName = orderFile.getName();
        if (partner == OuterOrderPartner.JD) {
            fileName = fileName.substring(0, fileName.indexOf("."));
        }
        List<LogisticImportData> logistics = new ArrayList<>();
        try {
            //准备转换器
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/ImportPartnerOrders/" + partner.toString().toLowerCase() + "Transfer.xml")
                    .inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("logistics", logistics);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderFile), beans);
            System.out.println(readStatus.isStatusOK());
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！并确保放在第一个sheet";
                Logger.info(errorInfo);
                render("ImportPartnerOrders/index.html", errorInfo, partner);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！并确保放在第一个sheet" + e.getMessage();
            e.printStackTrace();
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }
        List<String> existedOrderList = new ArrayList<>();
        List<String> notEnoughInventoryGoodsList = new ArrayList<>();
        Set<String> importSuccessOrderList = new HashSet<>();
        Set<String> unBindGoodsSet = new HashSet<>();
        List<String> diffOrderPriceList = new ArrayList<>();
        if (logistics.size() == 0) {
            errorInfo = "未发现有效数据！并确保放在第一个sheet";
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }
        processLogisticList(logistics, partner, existedOrderList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList, MANUAL_IMPORT_REAL_ORDER, fileName);
        render("ImportPartnerOrders/index.html", partner, errorInfo, existedOrderList, notEnoughInventoryGoodsList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList);
    }


    public static void autoCreateTaobaoRealOrder(Long resalerId) {
        Resaler resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        System.out.println(resalerId);
        if ("34".equals(resalerId)) {
            resaler = Resaler.findById(resalerId);
        }
        System.out.println(resaler);
        TaobaoClient taobaoClient = new DefaultTaobaoClient(KtvTaobaoUtil.URL, resaler.taobaoCouponAppKey,
                resaler.taobaoCouponAppSecretKey, Constants.FORMAT_JSON, 15000, 15000);
        //找到淘宝的token
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);

        TradesSoldGetRequest request = new TradesSoldGetRequest();
        request.setFields("orders.outer_iid,tid,orders.payment,orders.num,pay_time,orders.price,shipping_type," +
                "orders.logistics_company,receiver_mobile,receiver_phone,receiver_name,receiver_state,receiver_city," +
                "receiver_district,receiver_address,receiver_zip,has_buyer_message");
        request.setStatus("WAIT_SELLER_SEND_GOODS");
        long page = 1L;
        long pageSize = 100L;
        request.setUseHasNext(true);
        request.setPageSize(pageSize);

        String errorInfo = "";
        Set<String> unSetSupplierCodeList = new HashSet<>();
        List<LogisticImportData> logisticImportDataList = new ArrayList<>();
        while (true) {
            request.setPageNo(page);
            TradesSoldGetResponse response;
            try {
                response = taobaoClient.execute(request, token.accessToken);
                if (!response.isSuccess()) {
                    errorInfo = response.getSubCode();
                    break;
                } else {
                    List<Trade> tradeList = response.getTrades();
                    if (tradeList == null) {
                        tradeList = new ArrayList<>();
                    }
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
                            if (StringUtils.isBlank(order.getOuterIid())) {
                                Logger.info("该商品在淘宝上没有发货，请确认一下!" + trade.getTid());
                                unSetSupplierCodeList.add(String.valueOf(trade.getTid()));
                                continue;
                            }
                            logistic.setOuterGoodsNo(String.valueOf(order.getOuterIid()));
                            BigDecimal salePrice = new BigDecimal(order.getPayment()).divide(new BigDecimal(order.getNum()), RoundingMode.DOWN);
                            logistic.setSalePrice(salePrice);
                            logistic.setBuyNumber(order.getNum());
                            logistic.setPaidAt(trade.getPayTime());
                            logistic.setExpressInfo(order.getLogisticsCompany());
                            String receiverMobile = trade.getReceiverMobile();
                            if (StringUtils.isBlank(receiverMobile)) {
                                logistic.setPhone(trade.getReceiverPhone());
                            } else {
                                logistic.setPhone(receiverMobile);
                            }
                            logistic.setReceiver(trade.getReceiverName());
                            logistic.setZipCode(trade.getReceiverZip());
                            logistic.setRemarks(buyerMessage);
                            logistic.setAddress(trade.getReceiverState() + trade.getReceiverCity() + trade.getReceiverDistrict() + trade.getReceiverAddress());
                            logisticImportDataList.add(logistic);
                        }
                    }
                }
            } catch (ApiException e) {
                errorInfo = e.getErrMsg();
                break;
            }
            if (!response.getHasNext()) {
                break;
            }
            page += 1;
        }
        List<String> existedOrderList = new ArrayList<>();
        Set<String> importSuccessOrderList = new HashSet<>();
        Set<String> unBindGoodsSet = new HashSet<>();
        List<String> diffOrderPriceList = new ArrayList<>();
        processLogisticList(logisticImportDataList, OuterOrderPartner.TB, existedOrderList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList, TB_AUTO_IMPORT_REAL_ORDER, "");

        OuterOrderPartner partner = OuterOrderPartner.TB;
        render("ImportPartnerOrders/index.html", partner, errorInfo, existedOrderList,
                importSuccessOrderList, unBindGoodsSet, diffOrderPriceList, unSetSupplierCodeList);
    }

    private static void processLogisticList(List<LogisticImportData> logistics, OuterOrderPartner partner,
                                            List<String> existedOrderList, Set<String> importSuccessOrderList,
                                            Set<String> unBindGoodsSet, List<String> diffOrderPriceList,
                                            Boolean TBAutoImportRealOrder, String fileName) {
        //58订单处理
        if (partner == OuterOrderPartner.WB) {
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
                createWubaYbqOrder(logistic, partner, outerOrder, importSuccessOrderList, diffOrderPriceList, unBindGoodsSet);
            }
        } else {
            Resaler resaler = Resaler.findOneByLoginName(partner.partnerLoginName());
            Map<String, List<LogisticImportData>> outOrderMap = new HashMap<>();
            List<LogisticImportData> outGoodsNoList = new ArrayList<>();
            LogisticImportData newLogistic = null;
            for (LogisticImportData logistic : logistics) {
                if (partner == OuterOrderPartner.TB) {
                    OuterOrder outerOrder = OuterOrder.getOuterOrder(logistic.outerOrderNo, partner);
                    resaler = outerOrder.resaler;
                }
                if (partner == OuterOrderPartner.JD) {
                    logistic.outerGoodsNo = fileName;
                }
                Goods goods = null;
                if (TBAutoImportRealOrder) {
                    goods = ResalerProduct.getGoodsByOuterGoodsNo(resaler, logistic.outerGoodsNo, partner);
                } else {
                    goods = ResalerProduct.getGoodsByPartnerProductId(resaler, logistic.outerGoodsNo, partner);
                }
                if (goods == null) {
                    if (outOrderMap.containsKey(logistic.outerOrderNo)) {
                        outOrderMap = new HashMap<>();
                        Logger.info("该订单有未映射的商品，orderNO" + logistic.outerOrderNo);
                        continue;
                    }

                    //未映射商品
                    Logger.info("未映射商品NO=" + logistic.outerGoodsNo);
                    unBindGoodsSet.add(logistic.outerGoodsNo);
                    continue;
                }
                newLogistic = logistic;
//                if (unBindGoodsSet.size() > 0) {
//                    Logger.info("该订单有未映射的商品，outerGoodsNo" + logistic.outerGoodsNo);
//                    continue;
//                }

                if (outOrderMap.containsKey(logistic.outerOrderNo)) {
                    outGoodsNoList.add(newLogistic);
                    outOrderMap.put(logistic.outerOrderNo, outGoodsNoList);
                }

                if (outOrderMap.get(logistic.outerOrderNo) == null) {
                    outGoodsNoList = new ArrayList<>();
                    outGoodsNoList.add(newLogistic);
                    outOrderMap.put(logistic.outerOrderNo, outGoodsNoList);
                }
            }

            for (Map.Entry map : outOrderMap.entrySet()) {
                List<LogisticImportData> logisticList = (List<LogisticImportData>) map.getValue();
                String outerOrderNo = map.getKey().toString();
                Logger.info("Process OrderNO: %s", outerOrderNo);
                OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, outerOrderNo).first();
                if (outerOrder != null) {
                    if (outerOrder.orderType != OuterOrderType.IMPORT && outerOrder.status == OuterOrderStatus.ORDER_IGNORE) {
                        //如果 状态不是IMPORT， 并且status 是IGNORE，说明可能是一号店API拉去过来的订单，我们这里给转成导入的订单
                        outerOrder.message = new Gson().toJson(logisticList);
                        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                        outerOrder.orderType = OuterOrderType.IMPORT;
                    } else {
                        existedOrderList.add(outerOrderNo);
                        continue;
                    }
                } else {
                    outerOrder = toNewOuterOrder(outerOrderNo, partner, logisticList);
                }

                createYbqOrder(outerOrderNo, logisticList, outerOrder, partner, importSuccessOrderList, unBindGoodsSet, TBAutoImportRealOrder);
            }

        }

    }

    /**
     * 转换为 OuterOrder
     *
     * @param partner      分销伙伴
     * @param logisticList
     * @return OuterOrder
     */
    private static OuterOrder toNewOuterOrder(String outerOrderNo, OuterOrderPartner partner, List<LogisticImportData> logisticList) {
        OuterOrder outerOrder = new OuterOrder();
        outerOrder.orderId = outerOrderNo;
        outerOrder.partner = partner;
        if (partner == OuterOrderPartner.TB) {
            outerOrder.resaler = Resaler.findApprovedByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        } else {
            outerOrder.resaler = Resaler.findApprovedByLoginName(partner.partnerLoginName());
        }
        outerOrder.message = new Gson().toJson(logisticList);
        outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
        outerOrder.orderType = OuterOrderType.IMPORT;
        outerOrder.createdAt = new Date();
        return outerOrder;
    }

    private static void createWubaYbqOrder(LogisticImportData logistic, OuterOrderPartner partner, OuterOrder outerOrder,
                                           Set<String> importSuccessOrderList, List<String> diffOrderPriceList, Set<String> unBindGoodsSet) {
        //先取得订单总金额
        BigDecimal orderAmount = logistic.salePrice;
        Resaler wubaResaler = Resaler.findApprovedByLoginName(Resaler.WUBA_LOGIN_NAME);
        List<LogisticImportData> wubaLogistics = logistic.processWubaLogistic();
        if (wubaLogistics.size() == 0) {
            Logger.info("未映射商品NO=" + logistic.outerGoodsNo + " NOT Found!");
            unBindGoodsSet.add(logistic.outerGoodsNo);
            outerOrder.delete();
            return;
        }
        Order ybqOrder = logistic.createYbqOrderByWB(partner);
        //save ybqOrder info
        if (ybqOrder == null) {
            Logger.info("import wuba order,ybqOrder create fail");
            return;
        }
        outerOrder.ybqOrder = ybqOrder;
        outerOrder.save();
        ybqOrder.paidAt = logistic.paidAt;
        ybqOrder.createdAt = new Date();
        ybqOrder.save();


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
            } else {
                importSuccessOrderList.add(logistic.outerOrderNo);
            }
            nowOrderAmount = nowOrderAmount.add(goods.salePrice.multiply(new BigDecimal(wubaGoodsInfo.buyNumber)));
            //产生orderItem
            logistic.createOrderItem(ybqOrder, goods, orderShipInfo, wubaGoodsInfo.buyNumber, goods.salePrice);
        }

        //订单价格和商品单价*数量不一致的场合
        if (orderAmount.compareTo(nowOrderAmount) != 0) {
//            diffOrderPriceList.add(logistic.outerOrderNo);
            Logger.info("订单价格和商品单价*数量不一致的,订单NO=" + logistic.outerOrderNo);
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

    private static void createYbqOrder(String outerOrderNo, List<LogisticImportData> logisticList, OuterOrder outerOrder, OuterOrderPartner partner,
                                       Set<String> importSuccessOrderList, Set<String> unBindGoodsSet,
                                       Boolean TBAutoImportRealOrder) {
        Resaler resaler = Resaler.findOneByLoginName(partner.partnerLoginName());
        if (resaler == null) {
            Logger.error("can not find the resaler by login name: %s", partner.partnerLoginName());
            return;
        }

        Order ybqOrder = Order.createResaleOrder(resaler).save();
        if (ybqOrder == null) {
            Logger.info("import order,ybqOrder create fail");
            return;
        }

        //save ybqOrder info
        outerOrder.ybqOrder = ybqOrder;
        outerOrder.save();
        ybqOrder.paidAt = logisticList.get(0).paidAt;
        ybqOrder.createdAt = logisticList.get(0).paidAt;
        ybqOrder.save();
        importSuccessOrderList.add(outerOrderNo);
        OrderShippingInfo orderShipInfo = logisticList.get(0).createOrderShipInfo();
        BigDecimal nowOrderAmount = BigDecimal.ZERO;
        //创建OrderItem
        for (LogisticImportData logistic : logisticList) {
            Goods goods = null;
            if (TBAutoImportRealOrder) {
                goods = ResalerProduct.getGoodsByOuterGoodsNo(resaler, logistic.outerGoodsNo, partner);
            } else {
                goods = ResalerProduct.getGoodsByPartnerProductId(resaler, logistic.outerGoodsNo, partner);
            }
            nowOrderAmount = nowOrderAmount.add(goods.salePrice.multiply(new BigDecimal(logistic.buyNumber)));
            //产生orderItem
            logistic.createOrderItem(ybqOrder, goods, orderShipInfo, logistic.buyNumber, goods.salePrice);
        }

        ybqOrder.deliveryType = DeliveryType.LOGISTICS;
        ybqOrder.accountPay = ybqOrder.needPay;
        ybqOrder.discountPay = BigDecimal.ZERO;
        ybqOrder.payMethod = PaymentSource.getBalanceSource().code;
        Account account = ybqOrder.chargeAccount();
        ybqOrder.paid(account);
        ybqOrder.paidAt = logisticList.get(0).paidAt;
        ybqOrder.save();

    }

}
