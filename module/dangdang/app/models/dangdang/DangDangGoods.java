/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.dangdang;


import models.sales.Goods;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dangdang_goods")
public class DangDangGoods extends Model {

    public String spgid; //当当商品ID
    public Goods goods;

    /**
     * 返回一百券系统中商品总销量
     *
     * @return
     */
    public static void syncSellCount(Goods goods) {
        //todo


    }
}
