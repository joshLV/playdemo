package controllers;

import models.order.LogisticImportData;
import models.order.OrderItems;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
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
@ActiveNavigation("import_order_shipping_index")
public class ImportOrderShippingInfos extends Controller {
    @ActiveNavigation("import_order_shipping_index")
    public static void index() {
        render();
    }

    /**
     * 导入发货单
     *
     * @return
     */
    @ActiveNavigation("import_order_shipping_index")
    public static void upload(File orderShippingFile, Long supplierId) {
        String errorInfo = "";
        if (orderShippingFile == null) {
            errorInfo = "请先选择文件！";
            render("ImportOrderShippingInfos/index.html", errorInfo, supplierId);
        }
        List<LogisticImportData> logistics = new ArrayList<>();
        try {
            //准备转换器
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/OperateOrderShippingInfos/orderShippingTransfer.xml").inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("logistics", logistics);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderShippingFile), beans);
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！";
                Logger.info(errorInfo);
                render("OperateOrderShippingInfos/index.html", errorInfo, supplierId);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！" + e.getMessage();
            e.printStackTrace();
            render("ImportOrderShippingInfos/index.html", errorInfo, supplierId);
        }

        List<String> unExistedOrders = new ArrayList<>();
        List<String> uploadSuccessOrders = new ArrayList<>();
        for (LogisticImportData logistic : logistics) {
            System.out.println(logistic.expressCompany + ">>>>logistic");
            //查询该商户下的订单信息，存在则更新物流信息
            OrderItems orderItems = OrderItems.find("goods.id=? and goods.supplierId=? and order.orderNumber=?", Long.valueOf(logistic.goodsId), supplierId, logistic.orderNumber).first();
            if (orderItems == null) {
                unExistedOrders.add(logistic.orderNumber);
            } else {
//                orderItems.shippingInfo.expressCompany = logistic.expressCompany;
//                orderItems.shippingInfo.expressNumber = logistic.expressNumber;
//                orderItems.status = OrderStatus.SENT;
//                orderItems.save();
                uploadSuccessOrders.add(logistic.orderNumber);
            }
        }

        render("ImportOrderShippingInfos/index.html", supplierId);
    }


}
