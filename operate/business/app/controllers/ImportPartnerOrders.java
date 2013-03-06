package controllers;

import com.google.gson.Gson;
import models.order.PartnerOrderView;
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
    public static void upload(File orderFile, String partner) {

        if (orderFile == null) {
            String errorInfo = "请先选择文件！";
            render("ImportPartnerOrders/index.html", errorInfo);
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
                String errorInfo = "转换出错，请检查文件格式！";
                render("ImportPartnerOrders/index.html", errorInfo);
            }
        } catch (Exception e) {
            String errorInfo = "转换出现异常，请检查文件格式！";
            render("ImportPartnerOrders/index.html", errorInfo);
        }
        Logger.info("partnerOrderViews:\n%s",new Gson().toJson(partnerOrderViews));
    }
}
