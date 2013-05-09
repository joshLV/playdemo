package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.operator.Operator;
import operate.rbac.annotations.ActiveNavigation;
import play.Logger;
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
    public static void index(String name, String code) {
        List<Operator> operators = Operator.findByCondition(name, code);
        render(operators);
    }

    @ActiveNavigation("operator_search")
    public static void add() {
        render();
    }

    @ActiveNavigation("operator_search")
    public static void edit(Long id) {
        Operator operator = Operator.findById(id);
        render(operator);
    }


    @ActiveNavigation("operator_search")
    public static void create(@Valid Operator operator) {
        if (Validation.hasErrors()) {
            render("Operators/add.html", operator);
        }

        operator.createdBy = OperateRbac.currentUser().loginName;
        operator.createdAt = new Date();
        operator.deleted = DeletedStatus.UN_DELETED;
        operator.save();
        index(null, null);
    }

    @ActiveNavigation("operator_search")
    public static void update(Long id, @Valid Operator operator) {
        if (Validation.hasErrors()) {
//            operator.id = id;
            render("Operators/edit.html", operator);
        }
        // 更新用户信息
        Operator.update(id, operator, OperateRbac.currentUser().loginName);

        index(null, null);
    }

    @ActiveNavigation("operator_search")
    public static void delete(Long id) {
        Operator operator = Operator.findById(id);
        operator.deleted = DeletedStatus.DELETED;
        operator.save();
        index(null, null);
    }

    public static void checkNameAndCode(Long id, String name, String code) {
        String returnFlag = Operator.checkNameAndCode(id, name, code);
        renderJSON(returnFlag);
    }
}
