package models.order;

/**
 * 订单和订单项的状态，注意顺序是确定的，不可换
 */
public enum OrderStatus{
    UNPAID,
    PAID,
    CANCELED
}
