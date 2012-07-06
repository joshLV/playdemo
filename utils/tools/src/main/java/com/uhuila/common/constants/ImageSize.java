package com.uhuila.common.constants;

/**
 * 图片尺寸规格.
 * <p/>
 * User: sujie
 * Date: 3/21/12
 * Time: 11:49 AM
 */
public enum ImageSize {
    // nw 代表 no watermark
    // 不指定大小就不用写大小
    TINY    ("60x46_nw"),
    SMALL   ("172x132"),
    MIDDLE  ("234x178"),
    LARGE   ("340x260"),
    LOGO    ("300x180_nw"),
    SLIDE   ("nw"),
    ORIGINAL("nw"),
    DEFAULT ("");
    
    private String value;
    private ImageSize(String value){
        this.value=value;
    }
    
    public String toString(){
        return value;
    }
}
