package controllers;

import models.resale.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.Scope;
import play.mvc.With;

@With(OperateRbac.class)
@ActiveNavigation("resalers_index")
public class Resalers extends Controller {
	public static int PAGE_SIZE = 15;
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
	 * @param id 分销商ID
	 */
	public static void detail(Long id,String flag) {
		Resaler resaler = Resaler.findById(id);
		render(resaler,flag);
	}

	/**
	 * 审核分销商
	 * @param id 分销商ID
	 * @param status 状态
	 * @param remark 备注
	 */
	public static void update(Long id,ResalerStatus status,ResalerLevel level,String remark, ResalerCreditable creditable) {
		if (status == ResalerStatus.UNAPPROVED) {
			level = null;
		}
		Resaler.update(id,status,level,remark, creditable);
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
