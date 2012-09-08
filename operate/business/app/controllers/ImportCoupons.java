package controllers;

import models.sales.*;
import operate.rbac.annotations.ActiveNavigation;
import play.mvc.Controller;
import play.mvc.With;

import javax.persistence.PersistenceException;
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
        String toBeInsert = "";
        int lineNumber = 1;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(couponfile)));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null){
                lineNumber ++;
                line = line.trim();
                if(line.equals("")){
                    continue;
                }
                toBeInsert = line;
                new ImportedCoupon(goods, line).save();
                count ++;
            }
            goods.baseSale += count;
            goods.save();
        } catch (IOException e) {
            index("读取文本文件失败");
        }catch (PersistenceException e){
            index("第 " + lineNumber +  "行，券号 " +toBeInsert + " 无法重复插入");
        }
        index(null);
    }
}
