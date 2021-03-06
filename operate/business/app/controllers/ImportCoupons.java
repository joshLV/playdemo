package controllers;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import models.sales.DuplicatedImportedCoupon;

import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 12-9-8
 */
@With(OperateRbac.class)
@ActiveNavigation("pre_coupons")
public class ImportCoupons extends Controller {
    @ActiveNavigation("pre_coupons")
    public static void index(String errmsg, String d1, String d2) {
        List<models.sales.Goods> goodsList = models.sales.Goods.find("byCouponTypeAndDeleted", GoodsCouponType.IMPORT, DeletedStatus.UN_DELETED).fetch();
        render(goodsList, errmsg, d1, d2);
    }

    @ActiveNavigation("pre_coupons")
    public static void upload(Long goodsId, String action, File couponfile) {
        if (goodsId == null) {
            index("请选择商品", null, null);
        } else if (action == null || (!action.equals("append") && !action.equals("overwrite"))) {
            index("请选择上传方式", null, null);
        } else if (couponfile == null) {
            Logger.info("Not pass couponfile param.");
            index("请选择上传文件", null, null);
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if (goods == null) {
            index("您选择的商品不存在", null, null);
            return;
        }
        if ("overwrite".equals(action)) {
            ImportedCoupon.delete("goods = ? and status = ?",
                    goods, ImportedCouponStatus.UNUSED);
        }

        // 将所有非空数据trim后插入到临时表里
        int insertCount = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(couponfile)));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    continue;
                }
                if (line.contains(",")) {
                    String[] coupon = line.split(",");
                    new ImportedCouponTemp(goods, coupon[0], coupon[1]).save();
                } else {
                    new ImportedCouponTemp(goods, line).save();
                }
                insertCount++;
                if (insertCount % 50 == 0) {
                    JPA.em().flush();
                }
            }
        } catch (IOException e) {
            index("读取文本文件失败", null, null);
        }

        int duplicateCount = 0;


        //找出并删除导入的数据中的重复记录
        Query query = JPA.em().createQuery("select new models.sales.DuplicatedImportedCoupon(i.coupon,i.password) from ImportedCouponTemp i where i.goods.id = ? group by i.coupon having count(i.password) > 1");
        query.setParameter(1, goodsId);
        List<DuplicatedImportedCoupon> duplicateCouponsListInTemp = query.getResultList();
        List<String> duplicateCouponsInTemp = new ArrayList<>();
        if (duplicateCouponsListInTemp != null && duplicateCouponsListInTemp.size() > 0) {
            for (DuplicatedImportedCoupon coupon : duplicateCouponsListInTemp) {
                List<ImportedCouponTemp> couponTemps = ImportedCouponTemp.find("byGoodsAndCouponAndPassword", goods, coupon.coupon, coupon.password).fetch();
                if (couponTemps.size() > 1) {
                    for (int i = 1; i < couponTemps.size(); i++) {
                        if (StringUtils.isNotBlank(couponTemps.get(i).password)) {
                            duplicateCouponsInTemp.add(couponTemps.get(i).coupon + "," + couponTemps.get(i).password);
                        } else {
                            duplicateCouponsInTemp.add(couponTemps.get(i).coupon);
                        }
                        couponTemps.get(i).delete();
                        duplicateCount += 1;
                    }
                }
            }
        }


        //找出并删除导入的数据中与已有的数据相比重复的部分
        query = JPA.em().createQuery("select it from ImportedCouponTemp it, ImportedCoupon i " +
                "where it.goods = :goods and it.goods = i.goods and it.coupon = i.coupon and it.password = i.password ");
        query.setParameter("goods", goods);
        List<ImportedCouponTemp> duplicateWithIC = query.getResultList();
        List<String> duplicateCouponsWithIC = new ArrayList<>();
        for (ImportedCouponTemp ict : duplicateWithIC) {
            if (StringUtils.isNotBlank(ict.password)) {
                duplicateCouponsWithIC.add(ict.coupon + "," + ict.password);
            } else {
                duplicateCouponsWithIC.add(ict.coupon);
            }
            duplicateCount += 1;
            ict.delete();
        }


        //将临时表中所有数据导入到正式表中，并把临时表清空
        List<ImportedCouponTemp> allInTemp = ImportedCouponTemp.findAll();
        insertCount = 0;
        for (ImportedCouponTemp ict : allInTemp) {
            new ImportedCoupon(goods, ict.coupon, ict.password).save();
            insertCount += 1;
            if (insertCount % 20 == 0) {
                JPA.em().flush();
            }
        }
        ImportedCouponTemp.deleteAll();

        goods.refresh();
        if ("overwrite".equals(action)) {
            goods.cumulativeStocks = insertCount + goods.getRealSaleCount();
        } else {
            goods.cumulativeStocks += insertCount;
        }

        goods.save();
        index("无", StringUtils.join(duplicateCouponsInTemp, ";  "), StringUtils.join(duplicateCouponsWithIC, ";  "));
    }
}
