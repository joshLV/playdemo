package controllers;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.accounts.SettlementStatus;
import models.order.Prepayment;
import models.order.PrepaymentHistory;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static play.Logger.warn;

/**
 * 预付款.
 * <p/>
 * User: sujie
 * Date: 11/22/12
 * Time: 11:52 AM
 */
@With(OperateRbac.class)
@ActiveNavigation("prepayments_index")
public class OperatePrepayments extends Controller {

    public static int PAGE_SIZE = 15;

    /**
     * 获取券号列表页.
     */
    @ActiveNavigation("prepayments_index")
    public static void index(Long supplierId) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator<Prepayment> prepaymentPage = Prepayment.getPage(pageNumber, PAGE_SIZE, supplierId);

        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(prepaymentPage, supplierList, supplierId);
    }

    public static void show(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        render(prepayment);
    }

    /**
     * 显示添加页面
     */
    @ActiveNavigation("prepayments_add")
    public static void add() {
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(supplierList);
    }

    @ActiveNavigation("prepayments_add")
    public static void create(@Valid Prepayment prepayment) {
        String loginName = OperateRbac.currentUser().loginName;

        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            for (String key : validation.errorsMap().keySet()) {
                warn("create:      validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperatePrepayments/add.html", supplierList);
        }

        prepayment.deleted = DeletedStatus.UN_DELETED;
        prepayment.settlementStatus = SettlementStatus.UNCLEARED;
        prepayment.createdAt = new Date();
        prepayment.createdBy = loginName;
        prepayment.expireAt = DateUtil.getEndOfDay(prepayment.expireAt);

        prepayment.create();

        prepayment.refresh();
        Prepayment.toHistoryData(prepayment.id, loginName);
        index(null);
    }

    public static void edit(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        List<Supplier> supplierList = Supplier.findUnDeleted();
        render(prepayment, supplierList, id);
    }

    public static void update(Long id, @Valid Prepayment prepayment) {
        String loginName = OperateRbac.currentUser().loginName;

        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("update: validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperatePrepayments/edit.html", prepayment, id);
        }

        Prepayment.update(id, prepayment, loginName);

        Prepayment.toHistoryData(id, loginName);
        index(null);
    }

    public static void settle(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        Supplier supplier = Supplier.findById(prepayment.id);
        BigDecimal amount = prepayment.getSalesAmountUntilNow();
        BigDecimal prepaymentBalance = prepayment.amount;
        Long prepaymentId = prepayment.id;
        render(supplier, amount, prepaymentBalance, prepaymentId);
    }

    public static void confirmSettle(Long id) {

    }

    public static void history(Long id) {
        List<PrepaymentHistory> historyList = PrepaymentHistory.find("prepaymentId=? order by id desc", id).fetch();
        render("OperatePrepayments/history.html", historyList);
    }

    public static void delete(Long id) {
        Prepayment prepayment = Prepayment.findById(id);
        if (prepayment != null) {
            prepayment.deleted = DeletedStatus.DELETED;
            prepayment.save();
        }
        index(null);
    }
}
