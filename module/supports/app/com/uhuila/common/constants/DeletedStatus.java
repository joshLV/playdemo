package com.uhuila.common.constants;

/**
 * 标记删除的枚举类型.
 * <p/>
 * User: sujie
 * Date: 2/27/12
 * Time: 9:50 AM
 */
public enum DeletedStatus {
    UN_DELETED(0), DELETED(1);

    private int value;

    DeletedStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
