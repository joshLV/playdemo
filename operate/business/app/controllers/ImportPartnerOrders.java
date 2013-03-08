package controllers;

import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.PartnerOrderView;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
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
    public static void upload(File orderFile, String partner) {
        String errorInfo = "";
        if (orderFile == null) {
            errorInfo = "请先选择文件！";
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }
        List<PartnerOrderView> partnerOrderViews = new ArrayList<>();
        try {
            //准备转换器
            InputStream inputXML = VirtualFile.fromRelativePath(
                    "app/views/ImportPartnerOrders/" + partner + "Transfer.xml").inputstream();
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            //准备javabean
            Map<String, Object> beans = new HashMap<>();
            beans.put("partnerOrderViews", partnerOrderViews);

            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderFile), beans);
            if (!readStatus.isStatusOK()) {
                errorInfo = "转换出错，请检查文件格式！";
                render("ImportPartnerOrders/index.html", errorInfo, partner);
            }
        } catch (Exception e) {
            errorInfo = "转换出现异常，请检查文件格式！";
            render("ImportPartnerOrders/index.html", errorInfo, partner);
        }

        int duplicateCount = 0;
        List<String> orderList = new ArrayList<>();
        List<String> goodsList = new ArrayList<>();
        String preGoodsNo = "";
        String duplicateInfo = "";
        for (PartnerOrderView view : partnerOrderViews) {
            try {
                OuterOrder outerOrder = view.toOuterOrder(OuterOrderPartner.valueOf(partner.toUpperCase()));
                Order ybqOrder = view.toYbqOrder(OuterOrderPartner.valueOf(partner.toUpperCase()));
                if (ybqOrder != null) {
                    outerOrder.ybqOrder = ybqOrder;
                    outerOrder.save();
                    ybqOrder.paidAt = view.paidAt;
                    ybqOrder.createdAt = view.paidAt;
                    ybqOrder.save();
                    renderArgs.put("successInfo", "外部订单导入完毕！");
                } else {
                    if (!preGoodsNo.equals(view.outerGoodsNo)) {
                        goodsList.add(view.outerGoodsNo);
                        preGoodsNo = view.outerGoodsNo;
                    }
                    renderArgs.put("notExistGoodsInfo", "请检查以下渠道商品ID是否已映射一百券商品ID！");
                }
            } catch (Exception e) {
                orderList.add(view.outerOrderNo);
                duplicateInfo = "重复订单:" + (duplicateCount++) + "个,";
            }
        }

        renderArgs.put("notExistGoods", StringUtils.join(goodsList, ","));
        renderArgs.put("existedOrders", StringUtils.join(orderList, ","));
        render("ImportPartnerOrders/index.html", partner, duplicateInfo, errorInfo, duplicateCount);
    }
}
