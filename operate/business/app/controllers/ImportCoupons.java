package controllers;

import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.*;
import java.util.List;

/**
 * @author likang
 *         Date: 12-9-8
 */
@With(OperateRbac.class)
@ActiveNavigation("pre_coupons")
public class ImportCoupons extends Controller{
    @ActiveNavigation("pre_coupons")
    public static void index(String errmsg){
        List<models.sales.Goods> goodsList = models.sales.Goods.find("byCouponType",GoodsCouponType.IMPORT).fetch();
        render(goodsList, errmsg);
    }

    public static void upload(Long goodsId, String action, File couponfile){
        if(goodsId == null){
            index("请选择商品");
        }else if( action == null || (!action.equals("append") && !action.equals("overwrite"))){
            index("请选择上传方式");
        }else if(couponfile == null){
            index("请选择上传文件");
        }
        models.sales.Goods goods = models.sales.Goods.findById(goodsId);
        if(goods == null){
            index("您选择的商品不存在");
            return;
        }
        if("overwrite".equals(action)){
            goods.baseSale -= ImportedCoupon.delete("status = ?", ImportedCouponStatus.UNUSED);
        }

        // 将所有非空数据trim后插入到临时表里
        int insertCount = 0;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(couponfile)));
            String line;
            while ((line = reader.readLine()) != null){
                line = line.trim();
                if(line.equals("")){
                    continue;
                }
                new ImportedCouponTemp(goods, line).save();
                insertCount ++;
                if (insertCount % 50 == 0) {
                    JPA.em().flush();
                }
            }
        } catch (IOException e) {
            index("读取文本文件失败");
        }

        int duplicateCount = 0;
        //找出并删除导入的数据中的重复记录
        Query query = JPA.em().createNativeQuery("select i.coupon from (select coupon from imported_coupons_temp where goods_id = ?) i group by i.coupon having count(i.coupon) > 1");
        query.setParameter(1, goodsId);
        List<String> duplicateCoupons =query.getResultList();
        if(duplicateCoupons != null && duplicateCoupons.size() > 0){
            for(String coupon: duplicateCoupons){
                List<ImportedCouponTemp> couponTemps = ImportedCouponTemp.find("byGoodsAndCoupon", goods, coupon).fetch();
                if(couponTemps.size()> 1){
                    for (int i = 1; i < couponTemps.size(); i++){
                        couponTemps.get(i).delete();
                        duplicateCount += 1;
                    }
                }
            }
        }

        //找出并删除导入的数据中与已有的数据相比重复的部分
        query = JPA.em().createQuery("select it.coupon from importedCouponsTemp it, importedCoupons i " +
                "where it.goods = :goods and it.goods = i.goods and it.coupon = i.coupon");
        query.setParameter("goods", goods);
        List<String> c = query.getResultList();

        query = JPA.em().createQuery("select ");



        goods.baseSale += insertCount - duplicateCount;
        goods.save();
        index(null);
    }
}
