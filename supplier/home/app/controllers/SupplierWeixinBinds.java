package controllers;

import com.uhuila.common.util.RandomNumberUtil;
import controllers.supplier.SupplierInjector;
import models.admin.SupplierUser;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User: tanglq
 * Date: 13-3-25
 * Time: 下午7:00
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierWeixinBinds extends Controller {

    /**
     * 查看微信绑定指导界面.
     */
    public static void index() {
        SupplierUser supplierUser = SupplierUser.findById(SupplierRbac.currentUser().id);
        String qrCodePath = Play.configuration.getProperty("weixin.qrcode.path");

        if (StringUtils.isNotBlank(supplierUser.weixinOpenId)) {
            renderTemplate("SupplierWeixinBinds/unbindIndex.html", supplierUser, qrCodePath);
        }
        if (StringUtils.isBlank(supplierUser.idCode)) {
            supplierUser.idCode = RandomNumberUtil.generateSerialNumber(6);
            supplierUser.save();
        }
        render(supplierUser, qrCodePath);
    }

    /**
     * 解除微信帐号绑定.
     */
    public static void unbind() {
        SupplierUser supplierUser = SupplierUser.findById(SupplierRbac.currentUser().id);
        supplierUser.weixinOpenId = null;
        supplierUser.save();
        index();
    }
}
