package controllers;

import static play.Logger.warn;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import models.order.DiscountCode;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.ModelPaginator;
import play.mvc.Controller;
import play.mvc.With;
import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.FileUploadUtil;

@With(OperateRbac.class)
@ActiveNavigation("discountcode_index")
public class OperateDiscountCodes extends Controller {

    
    public static int PAGE_SIZE = 15;
    
    /**
     * 获取券号列表页.
     */
    public static void index() {
        String page = request.params.get("page");
        String discountSN = request.params.get("sn");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);
        ModelPaginator discountCodePage = DiscountCode.getDiscountCodePage(pageNumber, PAGE_SIZE, discountSN);
        render(discountCodePage, discountSN);
    }
    
    /**
     * 添加品牌
     */
    @ActiveNavigation("discountcode_add")
    public static void add() {
        DiscountCode brand = new DiscountCode();
        render(brand);
    }
    
    @ActiveNavigation("discountcode_add")
    public static void create(@Valid DiscountCode discountCode) {
        if (Validation.hasErrors()) {
            List<Supplier> supplierList = Supplier.findUnDeleted();
            for (String key : validation.errorsMap().keySet()) {
                warn("create:      validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperateDiscountCodes/add.html", supplierList);
        }
        discountCode.deleted = DeletedStatus.UN_DELETED;
        discountCode.create();
    
        index();
    }

    public static void edit(Long id) {
        DiscountCode discountCode = DiscountCode.findById(id);
    
        render(discountCode, id);
    }
    
    public static void update(Long id, File logoImage, @Valid DiscountCode discountCode) {
        if (Validation.hasErrors()) {
            for (String key : validation.errorsMap().keySet()) {
                warn("update:     validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
            }
            render("OperateDiscountCodes/edit.html", discountCode);
        }
    

        DiscountCode.update(id, discountCode);
    
        index();
    }
    
    public static void delete(Long id) {
        DiscountCode discountCode = DiscountCode.findById(id);
        if (discountCode != null) {
            discountCode.deleted= DeletedStatus.DELETED;
            discountCode.save();
        }
        index();
    }

}
