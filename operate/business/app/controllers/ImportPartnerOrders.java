package controllers;

import com.google.gson.Gson;
import models.order.*;
import models.resale.Resaler;
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
     * @param partner
     */
    public static void download(OuterOrderPartner partner){
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
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/ImportPartnerOrders/" + partner.toString().toLowerCase() + "Transfer.xml").inputstream();
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

        for (LogisticImportData logistic : logistics) {
            Logger.info("Process OrderNO: %s", logistic.outerOrderNo);
            OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, logistic.outerOrderNo).first();

            //TODO 相同订单,不同item的暂时先不作处理
            if (outerOrder != null) {
                if (outerOrder.orderType != OuterOrderType.IMPORT && outerOrder.status == OuterOrderStatus.ORDER_IGNORE){
                    //如果 状态不是IMPORT， 并且status 是IGNORE，说明可能是一号店API拉去过来的订单，我们这里给转成导入的订单
                    outerOrder.message = new Gson().toJson(logistic);
                    outerOrder.status = OuterOrderStatus.ORDER_SYNCED;
                    outerOrder.orderType = OuterOrderType.IMPORT;
                }else{
                    existedOrderList.add(logistic.outerOrderNo);
                    continue;
                }
            } else {
                outerOrder = logistic.toOuterOrder(partner);
            }
            Order ybqOrder;
            try {
                ybqOrder = logistic.toYbqOrder(partner);
            } catch (NotEnoughInventoryException e) {
                e.printStackTrace();
                notEnoughInventoryGoodsList.add(logistic.outerGoodsNo);
                JPA.em().getTransaction().rollback();
                importSuccessOrderList.clear();
                break;
            }
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
        }
        renderArgs.put("unBindGoodsList", unBindGoodsSet);
        renderArgs.put("importSuccessOrderList", importSuccessOrderList);
        renderArgs.put("notEnoughInventoryGoodsList", notEnoughInventoryGoodsList);
        renderArgs.put("existedOrderList", existedOrderList);
        render("ImportPartnerOrders/index.html", partner, errorInfo);
    }
}
