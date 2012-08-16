package controllers;

import com.uhuila.common.util.FileUploadUtil;
import models.sales.Goods;
import models.sales.SecKillGoodsCondition;
import models.sales.SecKillGoodsItem;
import models.sales.SecKillGoodsStatus;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-8-15
 * Time: 上午11:17
 */
@With(OperateRbac.class)
@ActiveNavigation("seckill_goods_index")
public class SecKillGoods extends Controller {
    public static int PAGE_SIZE = 15;

    /**
     * 展示商品一览页面
     */
    @ActiveNavigation("seckill_goods_index")
    public static void index(SecKillGoodsCondition condition) {
        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        if (condition == null) {
            condition = new SecKillGoodsCondition();

        }

        JPAExtPaginator<models.sales.SecKillGoods> goodsPage = models.sales.SecKillGoods.findByCondition(condition, pageNumber,
                PAGE_SIZE);
        goodsPage.setBoundaryControlsEnabled(true);

        render(goodsPage, condition);
    }


    /**
     * 展示添加秒杀页面
     */
    @ActiveNavigation("seckill_goods_add")
    public static void add() {
        render();
    }

    public static void create(@Valid models.sales.SecKillGoods secKillGoods, @Required File imagePath) {
        checkImageFile(imagePath);
        if (Validation.hasErrors()) {
            String goodsName = "商品名：" + secKillGoods.goods.name;
            render("SecKillGoods/add.html", goodsName);
        }

        secKillGoods.createdAt = new Date();
        secKillGoods.create();

        try {
            secKillGoods.imagePath = uploadImagePath(imagePath, secKillGoods.id, null);
        } catch (IOException e) {
            error(500, "secKillGoods.image_upload_failed");
        }
        secKillGoods.save();

        index(null);
        render();
    }

    public static void update(Long id, @Valid models.sales.SecKillGoods secKillGoods, File imagePath, String imageLargePath) {
        checkImageFile(imagePath);
        if (Validation.hasErrors()) {
            String goodsName = "商品名：" + secKillGoods.goods.name;
            render("SecKillGoods/add.html", goodsName);
            renderArgs.put("imageLargePath", imageLargePath);
            render("SecKillGoods/edit.html", secKillGoods, id);
        }

        try {
            models.sales.SecKillGoods oldGoods = models.sales.SecKillGoods.findById(id);
            String oldImagePath = oldGoods == null ? null : oldGoods.imagePath;
            String image = uploadImagePath(imagePath, id, oldImagePath);
            if (StringUtils.isNotEmpty(image)) {
                secKillGoods.imagePath = image;
            }
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
        }
        models.sales.SecKillGoods.update(id, secKillGoods);

        index(null);
    }

    public static void edit(Long id) {
        models.sales.SecKillGoods secKillGoods = models.sales.SecKillGoods.findById(id);
        renderArgs.put("imageLargePath", secKillGoods.getImageLargePath());
        String goodsName = "商品名：" + secKillGoods.goods.name;
        render(secKillGoods,goodsName, id);
    }

    private static void checkImageFile(File imagePath) {
        if (imagePath != null) {
            //检查目录
            File uploadDir = new File(UploadFiles.ROOT_PATH);
            if (!uploadDir.isDirectory()) {
                Validation.addError("secKillGoods.imagePath", "validation.write");
            }

            //检查目录写权限
            if (!uploadDir.canWrite()) {
                Validation.addError("secKillGoods.imagePath", "validation.write");
            }

            if (imagePath.length() > UploadFiles.MAX_SIZE) {
                Validation.addError("secKillGoods.imagePath", "validation.maxFileSize");
            }

            //检查扩展名
            //定义允许上传的文件扩展名
            String[] fileTypes = UploadFiles.FILE_TYPES.trim().split(",");
            String fileExt = imagePath.getName().substring(imagePath.getName().lastIndexOf(".") + 1).toLowerCase();
            if (!Arrays.<String>asList(fileTypes).contains(fileExt)) {
                Validation.addError("secKillGoods.imagePath", "validation.invalidType", StringUtils.join(fileTypes, ','));
            }
        }
    }

    /**
     * 上传图片
     *
     * @param uploadImageFile
     * @param goodsId
     */
    private static String uploadImagePath(File uploadImageFile, Long goodsId, String oldImageFile) throws IOException {
        if (uploadImageFile == null || uploadImageFile.getName() == null) {
            return "";
        }
        //取得文件存储路径
        String absolutePath = FileUploadUtil.storeImage(uploadImageFile, goodsId, UploadFiles.ROOT_PATH);
        if (oldImageFile != null && !"".equals(oldImageFile)) {
            File oldImage = new File(UploadFiles.ROOT_PATH + oldImageFile);
            oldImage.delete();
        }
        return absolutePath.substring(UploadFiles.ROOT_PATH.length(), absolutePath.length());

    }

    public static void checkGoodsId(Long id) {
        Goods goods = Goods.findById(id);
        if (goods != null) {
            renderText(goods.name);
        }
    }

    /**
     * 删除指定商品
     *
     * @param id 商品ID
     */
    public static void delete(Long id) {
        models.sales.SecKillGoods goods = models.sales.SecKillGoods.findById(id);
        //还在上架的秒杀活动不可以删除
        List<SecKillGoodsItem> items = SecKillGoodsItem.find("seckill_goods_id=?", id).fetch();
        for (SecKillGoodsItem item : items) {
            if (item.status == SecKillGoodsStatus.ONSALE) {
                index(null);
            } else {
                item.delete();
            }
        }
        goods.delete();
        index(null);
    }
}