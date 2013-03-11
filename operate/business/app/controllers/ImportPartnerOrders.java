package controllers;

import models.order.Logistic;
import models.order.NotEnoughInventoryException;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
import play.db.jpa.JPA;
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
        List<Logistic> logistics = new ArrayList<>();
        try {
            //准备转换器
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/ImportPartnerOrders/" + partner.toString().toLowerCase() + "Transfer.xml").inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("logistics", logistics);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderFile), beans);
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！";
                render("ImportPartnerOrders/index.html", errorInfo, partner);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！";
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }

        List<String> existedOrderList = new ArrayList<>();
        List<String> unBindGoodsList = new ArrayList<>();
        List<String> notEnoughInventoryGoodsList = new ArrayList<>();
        List<String> importSuccessOrderList = new ArrayList<>();

        String preGoodsNo = "";
        for (Logistic logistic : logistics) {
            OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, logistic.outerOrderNo).first();
            if (outerOrder != null) {
                existedOrderList.add(logistic.outerOrderNo);
                continue;
            } else {
                outerOrder = logistic.toOuterOrder(partner);
            }
            Order ybqOrder = null;
            try {
                ybqOrder = logistic.toYbqOrder(partner);
            } catch (NotEnoughInventoryException e) {
                notEnoughInventoryGoodsList.add(logistic.outerGoodsNo);
                JPA.em().getTransaction().rollback();
                importSuccessOrderList.clear();
                break;
            }
            if (ybqOrder != null) {
                outerOrder.ybqOrder = ybqOrder;
                outerOrder.save();
                ybqOrder.paidAt = logistic.paidAt;
                ybqOrder.createdAt = logistic.paidAt;
                ybqOrder.save();

                logistic.orderItems = ybqOrder.orderItems.get(0);
                logistic.save();
                importSuccessOrderList.add(logistic.outerOrderNo);
            } else {
                if (!preGoodsNo.equals(logistic.outerGoodsNo)) {
                    unBindGoodsList.add(logistic.outerGoodsNo);
                    preGoodsNo = logistic.outerGoodsNo;
                }
            }
        }

        renderArgs.put("unBindGoodsList", unBindGoodsList);
        renderArgs.put("importSuccessOrderList", importSuccessOrderList);
        renderArgs.put("notEnoughInventoryGoodsList", notEnoughInventoryGoodsList);
        renderArgs.put("existedOrderList", existedOrderList);
        render("ImportPartnerOrders/index.html", partner, errorInfo);
    }
}
