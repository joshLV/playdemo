package controllers;

import models.order.PartnerOrderView;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import operate.rbac.annotations.ActiveNavigation;
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
        try {
            InputStream inputXML = VirtualFile.fromRelativePath("app/views/ImportPartnerOrders/" + partner + "Transfer.xml").inputstream();
            List<PartnerOrderView> partnerOrderViews = new ArrayList<>();
            Map<String, Object> beans = new HashMap<>();
            beans.put("partnerOrderViews", partnerOrderViews);

            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            InputStream inputXLS = new FileInputStream(orderFile);
            XLSReadStatus readStatus = mainReader.read(inputXLS, beans);
            if (!readStatus.isStatusOK()) {
                error("转换失败.");
            } else {
                for (PartnerOrderView view : partnerOrderViews) {
                    System.out.println(view.zipCode+ ">>>>view.outerGoodsNo" + view.address);
                }
            }
        } catch (Exception e) {
            error(e);
        }
    }
}
