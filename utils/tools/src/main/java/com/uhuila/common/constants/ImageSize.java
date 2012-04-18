package com.uhuila.common.constants;

/**
 * 图片尺寸规格.
 * <p/>
 * User: sujie
 * Date: 3/21/12
 * Time: 11:49 AM
 */
public enum ImageSize {
    TINY("tiny"), SMALL("small"), MIDDLE("middle"), LARGE("large"),LOGO("logo"), ORIGINAL("");
    
    private String value;
    private ImageSize(String value){
        this.value=value;
    }
    
    public String toString(){
        return value;
    }
}
