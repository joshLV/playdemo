package controllers;

import models.operator.OperateUser;
import models.operator.Operator;
import models.resale.Resaler;
import models.resale.ResalerCondition;
import models.resale.ResalerStatus;
import operate.rbac.ContextedPermission;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With(OperateRbac.class)
@ActiveNavigation("resalers_index")
public class Resalers extends Controller {
    public static int PAGE_SIZE = 15;
    private static final String EDITOR_ROLE = "editor";

    /**
     * 查询分销商信息
     *
     * @param condition 查询条件
     */
    public static void index(ResalerCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        JPAExtPaginator<Resaler> resalers = Resaler.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        resalers.setBoundaryControlsEnabled(true);
        renderArgs.put("condition", condition);
        render(resalers);
    }

    /**
     * 分销商详细
     *
     * @param id 分销商ID
     */
    public static void detail(Long id, String flag) {
        List<OperateUser> operateUserList = OperateUser.getSales(EDITOR_ROLE);
        Boolean hasHandleResalerCommissionRatioPermission = ContextedPermission.hasPermission("HANDLE_RESALER_COMMISSIONRATIO");
        Resaler resaler = Resaler.findById(id);
        List<Operator> operators = Operator.findUnDeleted();
        render(resaler, flag, operateUserList, operators,hasHandleResalerCommissionRatioPermission);
    }

    /**
     * 审核分销商
     *
     * @param id 分销商ID
     */
    public static void update(Long id, Resaler resaler) {
        Resaler.update(id, resaler);
        index(null);
    }

    public static void check(long id, ResalerStatus status, String remark) {
        Resaler.updateStatus(id, status, remark);
        index(null);
    }

    public static void freeze(long id) {
        Resaler.freeze(id);
        index(null);
    }

    public static void unfreeze(long id) {
        Resaler.unfreeze(id);
        index(null);
    }

}
