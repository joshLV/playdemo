/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.sales;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
@Table(name = "goods")
public class Goods extends Model {

    private static final Pattern imagePat = Pattern.compile("^/o/([0-9]+)/([0-9]+)/([0-9]+)/([^_]+).(jpg|png|gif|jpeg)$");
    private static final String IMAGE_SERVER = "http://localhost:9007";

    /**
     * 商品编号
     */
    public String no;
    /**
     * 商品名称
     */
    public String name;
    /**
     * 所属商户ID
     */
    public String company_id;
    /**
     * 原始图片路径
     */
    public String image_path;
    /**
     * 中等规格图片路径
     */
    private String image_tiny_path;
    /**
     * 中等规格图片路径
     */
    private String image_small_path;
    /**
     * 中等规格图片路径
     */
    private String image_middle_path;
    /**
     * 中等规格图片路径
     */
    private String image_large_path;

    public String getImage_tiny_path() {
        return getImageBySizeType("tiny");
    }

    public String getImage_small_path() {
        return getImageBySizeType("small");
    }

    public String getImage_middle_path() {
        return getImageBySizeType("middle");
    }

    public String getImage_large_path() {
        return getImageBySizeType("large");
    }

    private String getImageBySizeType(String sizeType) {
        String defaultImage = IMAGE_SERVER + "/p/1/1/1/default_" + sizeType + ".png";
        if (image_path == null || image_path.equals("")){
            return defaultImage;
        }
        Matcher matcher = imagePat.matcher(image_path);
        if (!matcher.matches()) {

            return defaultImage;
        }
        String imageHeadStr = image_path.replace("/o/", "/p/");
        return IMAGE_SERVER + imageHeadStr.replace("/" + matcher.group(4), "/" + matcher.group(4) + "_" + sizeType);
    }


    /**
     * 进货量
     */
    public String income_goods_count;
    /**
     * 券有效开始日
     */
    public String expired_bg_on;
    /**
     * 券有效结束日
     */
    public String expired_ed_on;
    /**
     * 商品标题
     */
//    public String title;
    /**
     * 商品原价
     */
    public String original_price;
    /**
     * 商品现价
     */
    public String sale_price;
    /**
     * 温馨提示
     */
    public String prompt;
    /**
     * 商品详情
     */
    public String details;
    /**
     * 售出数量
     */
    public String sale_count;
    /**
     * 售出基数
     */
    public String base_sale;
    /**
     * 商品状态,
     */
    public String status;
    /**
     * 创建来源
     */
    public String created_from;
    /**
     * 创建时间
     */
    public String created_at;
    /**
     * 创建人
     */
    public String created_by;
    /**
     * 逻辑删除
     */
    public String deleted;
    /**
     * 乐观锁
     */
    public String lock_version;
    /**
     * 手工排序
     */
    public String display_order;


    public static List<Goods> findTopByCategory(int categoryId, int limit) {
        //todo 商品状态判断
        return find("").fetch(limit);
    }
}
