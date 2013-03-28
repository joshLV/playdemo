package controllers;

import play.mvc.Controller;
import controllers.supplier.SupplierInjector;

import play.mvc.With;

/**
 * 商户帮助页面
 * <p/>
 * User: wangjia
 * Date: 13-3-27
 * Time: 上午10:12
 */
@With({SupplierRbac.class, SupplierInjector.class})
public class SupplierHelps extends Controller {
    /**
     * 如何查看已经验证的券号
     */
    public static void verify() {
        render();
    }

    /**
     * 如何查看已经验证的券号
     */
    public static void viewVerifiedCoupons() {
        render();
    }

    /**
     * 如何查看账户资金变动
     */
    public static void viewAccountSequences() {
        render();
    }

    /**
     * 如何提现
     */
    public static void withdraw() {
        render();
    }

    /**
     * 如何查看销售业绩
     */
    public static void viewReports() {
        render();
    }

    /**
     * 如何管理商户门店信息
     */
    public static void manageShops() {
        render();
    }

    /**
     * 如何管理门店账号
     */
    public static void manageUsers() {
        render();
    }

    /**
     * 如何修改密码
     */
    public static void changePassword() {
        render();
    }


}
