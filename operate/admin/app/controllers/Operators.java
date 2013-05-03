package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.operator.OperateRole;
import models.operator.Operator;
import operate.rbac.annotations.ActiveNavigation;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 运营商管理.
 * <p/>
 * User: wangjia
 * Date: 13-5-3
 * Time: 下午3:16
 */
@With(OperateRbac.class)
@ActiveNavigation("operator_search")
public class Operators extends Controller {
    @ActiveNavigation("operator_search")
    public static void index(String name) {
        render();
    }

    @ActiveNavigation("operator_search")
    public static void add() {
        List rolesList = OperateRole.findAll();
        render(rolesList);
    }


    @ActiveNavigation("operator_search")
    public static void create(@Valid Operator operator) {
        if (Validation.hasErrors()) {
            render("Operators/add.html", operator);
        }
        System.out.println(operator.name + "《=========operator.name:");
        System.out.println(operator.phone + "《=========operator.phone:");
        System.out.println(operator.mobile + "《=========operator.mobile:");
        System.out.println(operator.email + "《=========operator.email:");
        System.out.println(operator.remark + "《=========operator.remark:");
        operator.createdBy = OperateRbac.currentUser().loginName;
        operator.createdAt = new Date();
        operator.deleted = DeletedStatus.UN_DELETED;
        operator.save();
        index(null);
    }

    public static void checkName(Long id, String name) {
        String returnFlag = Operator.checkValue(id, name);
        renderJSON(returnFlag);
    }
}
