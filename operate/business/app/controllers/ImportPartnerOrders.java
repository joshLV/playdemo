package controllers;

import com.google.gson.Gson;
<<<<<<<Updated upstream
        =======
import models.accounts.AccountType;
import models.order.Order;
import models.order.OuterOrder;
import models.order.OuterOrderPartner;
import models.order.OuterOrderStatus;
>>>>>>>Stashed changes
import models.order.PartnerOrderView;
import models.resale.Resaler;
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
        String msgInfo = "";
        if (orderFile == null) {
            msgInfo = "请先选择文件！";
            render("ImportPartnerOrders/index.html", msgInfo);
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

            //转换
            XLSReadStatus readStatus = mainReader.read(new FileInputStream(orderFile), beans);
            if (!readStatus.isStatusOK()) {
                msgInfo = "转换出错，请检查文件格式！";
                render("ImportPartnerOrders/index.html", msgInfo);
            }
        } catch (Exception e) {
            msgInfo = "转换出现异常，请检查文件格式！";
            render("ImportPartnerOrders/index.html", msgInfo);
        }


        int duplicateCount = 0;
        List<String> orderList = new ArrayList<>();
        for (PartnerOrderView view : partnerOrderViews) {
            OuterOrder outerOrder = OuterOrder.find("byPartnerAndOrderId", partner, view.outerOrderNo).first();
            if (outerOrder == null) {
                outerOrder = new OuterOrder();
                outerOrder.orderId = view.outerOrderNo;
                outerOrder.partner = OuterOrderPartner.DD;
                outerOrder.message = new Gson().toJson(view);
                outerOrder.status = OuterOrderStatus.ORDER_COPY;
                outerOrder.save();
                view.toYbqOrder(partner);
            } else {
                duplicateCount++;
                orderList.add(view.outerOrderNo);
            }
        }
        msgInfo = "外部订单导入完毕！";
        render("ImportPartnerOrders/index.html", msgInfo, duplicateCount);
    }

}
