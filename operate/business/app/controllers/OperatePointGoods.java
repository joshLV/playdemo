package controllers;

import com.uhuila.common.util.FileUploadUtil;
import models.resale.ResalerLevel;
import models.sales.*;
import models.sales.Goods;
import models.supplier.Supplier;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static play.Logger.warn;

/**
 * Created with IntelliJ IDEA.
 * User: clara
 * Date: 12-8-2
 * Time: 下午5:17
 * To change this template use File | Settings | File Templates.
 */

@With(OperateRbac.class)
@ActiveNavigation("point_goods_index")
public class OperatePointGoods extends Controller {

    public static int PAGE_SIZE = 15;
    public static String WWW_URL = Play.configuration.getProperty("www.url", "");


    /**
     * 展示商品一览页面
     */
    @ActiveNavigation("point_goods_index")
    public static void index(models.sales.GoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

//        if (condition == null) {
//            condition = new GoodsCondition();
//            condition.status = GoodsStatus.ONSALE;
//        }
//
//        if (condition.priority == 1) {
//            condition.orderBy = "g.priority";
//        } else {
//            condition.orderBy = "g.createdAt";
//        }
        //JPAExtPaginator<PointGoods> pointGoodsPage = models.sales.PointGoods.findByCondition(condition, pageNumber, PAGE_SIZE);


        JPAExtPaginator<PointGoods> pointGoodsPage = new JPAExtPaginator<>(PointGoods.class);

        pointGoodsPage.setPageNumber(pageNumber);
        pointGoodsPage.setPageSize(PAGE_SIZE);


        pointGoodsPage.setBoundaryControlsEnabled(true);


        // render(goodsPage, supplierList, condition);
        render(pointGoodsPage);
    }


    /**
     * 展示添加商品页面
     */
    @ActiveNavigation("point_goods_add")
    public static void add() {
        renderInit(null);
        render();
    }

    /**
     * 初始化form界面.
     * 添加和修改页面共用
     *
     * @param pointGoods
     */
    private static void renderInit(models.sales.PointGoods pointGoods) {
        if (pointGoods == null) {
            pointGoods = new models.sales.PointGoods();
            pointGoods.materialType = MaterialType.ELECTRONIC;
            renderArgs.put("pointGoods.materialType", MaterialType.ELECTRONIC);
        }

        //调试用
        for (String key : validation.errorsMap().keySet()) {
            warn("validation.errorsMap().get(" + key + "):" + validation.errorsMap().get(key));
        }
        renderArgs.put("pointGoods", pointGoods);
    }


    /**
     * 添加商品
     * 商户只能添加电子券.
     *
     * @param imagePath
     * @param pointGoods
     */
    @ActiveNavigation("point_goods_add")
    public static void create(@Valid models.sales.PointGoods pointGoods, @Required File imagePath) {

        checkImageFile(imagePath);

        checkExpireAt(pointGoods);

        checkPointPrice(pointGoods);

        if (Validation.hasErrors()) {
            renderInit(pointGoods);
            //   boolean selectAll = false;
            //  render("OperateGoods/add.html", selectAll);
            render("OperatePointGoods/add.html");
        }

        //预览
        if (GoodsStatus.UNCREATED.equals(pointGoods.status)) {
            preview(null, pointGoods, imagePath);
        }


        // goods.createdBy = OperateRbac.currentUser().loginName;

        pointGoods.create();
        try {
            pointGoods.imagePath = uploadImagePath(imagePath, pointGoods.id, null);
        } catch (IOException e) {
            error(500, "goods.image_upload_failed");
        }
        pointGoods.save();

        index(null);
    }


    private static void checkImageFile(File imagePath) {
        if (imagePath != null) {
            //检查目录
            File uploadDir = new File(UploadFiles.ROOT_PATH);
            if (!uploadDir.isDirectory()) {
                Validation.addError("pointGoods.imagePath", "validation.write");
            }

            //检查目录写权限
            if (!uploadDir.canWrite()) {
                Validation.addError("pointGoods.imagePath", "validation.write");
            }

            if (imagePath.length() > UploadFiles.MAX_SIZE) {
                Validation.addError("pointGoods.imagePath", "validation.maxFileSize");
            }

            //检查扩展名
            //定义允许上传的文件扩展名
            String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
            String fileExt = imagePath.getName().substring(imagePath.getName().lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
                Validation.addError("pointGoods.imagePath", "validation.invalidType", StringUtils.join(fileTypes, ','));
            }
        }
    }


    /**
     * 上传图片
     *
     * @param uploadImageFile
     * @param pointGoodsId
     */
    private static String uploadImagePath(File uploadImageFile, Long pointGoodsId, String oldImageFile) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径
        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, pointGoodsId, UploadFiles.ROOT_PATH);
        if (oldImageFile != null && !"".equals(oldImageFile)) {
            File oldImage = new File(UploadFiles.ROOT_PATH + oldImageFile);
            oldImage.delete();
        }
        return absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());
    }

    /**
     * 展示添加商品页面
     */

    private static void preview(Long pointGoodsId, PointGoods pointGoods, File imagePath) {
        String cacheId = "0";
        try {
            cacheId = PointGoods.preview(pointGoodsId, pointGoods, imagePath, UploadFiles.ROOT_PATH);
        } catch (IOException e) {
            e.printStackTrace();
            error(500, "goods.image_upload_failed");
        }
        cacheId = play.cache.Cache.get(cacheId.toString()).toString();
        redirect("http://" + WWW_URL + "/g/" + cacheId + "?preview=true");
    }


    private static void checkPointPrice(PointGoods pointGoods) {
        if (pointGoods.pointPrice == null) {
            Validation.addError("pointGoods.pointPrice", "validation.required");
        }
        if (pointGoods.originalPrice != null) {
            Validation.addError("pointGoods.originalPrice", "validation.required");
        }
        if (pointGoods.pointPrice != null && pointGoods.originalPrice != null && pointGoods.pointPrice.compareTo(pointGoods.originalPrice) < 0) {
            Validation.addError("pointGoods.pointPrice", "validation.lessThanOriginalPrice");
        }
    }


    private static void checkExpireAt(PointGoods pointGoods) {
        if (pointGoods.effectiveAt != null &&pointGoods.expireAt != null &&pointGoods.expireAt.before(pointGoods.effectiveAt)) {
            Validation.addError("pointGoods.expireAt", "validation.beforeThanEffectiveAt");
        }


    }

}

