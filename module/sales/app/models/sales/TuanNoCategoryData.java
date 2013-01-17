package models.sales;

import java.io.Serializable;

/**
 * User: wangjia
 * Date: 12-11-26
 * Time: 上午11:44
 */
public class TuanNoCategoryData implements Serializable {
    public String name;
    public Long categoryId;

    public static TuanNoCategoryData from(Category category) {
        TuanNoCategoryData data = new TuanNoCategoryData();
        data.categoryId = category.id;
        data.name = category.name;
        return data;
    }
}
