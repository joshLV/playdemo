/*
 * Copyright 2012 uhuila.com, Inc. All rights reserved.
 * uhuila.com PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package models.dangdang;

import models.order.Order;

public class DangDangOrder{

    public Order order;
    
    public void handleOrder(){
        createAndUpdateInventory();
        payAndSendECoupon();
    }
    public void createAndUpdateInventory() {
    }

    public void payAndSendECoupon() {

    }
}