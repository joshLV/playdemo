package controllers;

import models.order.ExpressCompany;
import models.order.LogisticImportData;
import models.order.OrderItems;
import models.order.OrderStatus;
import models.supplier.Supplier;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.With;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
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
            //查询该商户下的订单信息，存在则更新物流信息
            OrderItems orderItems = OrderItems.find("goods.sku is not null and goods.id=? and order.orderNumber=?", Long.valueOf(logistic.goodsId), logistic.orderNumber).first();
            System.out.println(orderItems + ">>>>orderItems");
            if (orderItems == null) {
                unExistedOrders.add(logistic.orderNumber);
                continue;
            }
            orderItems.shippingInfo.expressCompany = expressCompany;
            orderItems.shippingInfo.expressNumber = logistic.expressNumber;
            orderItems.shippingInfo.save();
            orderItems.status = OrderStatus.SENT;
            orderItems.save();
            uploadSuccessOrders.add(logistic.orderNumber);
        }

        List<ExpressCompany> expressCompanyList = ExpressCompany.findAll();
        renderArgs.put("emptyExpressInofs", emptyExpressInofs);
        renderArgs.put("expressCompanyList", expressCompanyList);
        renderArgs.put("unExistedOrders", unExistedOrders);
        renderArgs.put("unExistedExpressCompanys", unExistedExpressCompanys);
        renderArgs.put("uploadSuccessOrders", uploadSuccessOrders);
        render("UploadOrderShippingInfos/index.html", supplierList);
    }


}
