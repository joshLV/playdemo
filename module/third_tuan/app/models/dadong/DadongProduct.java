package models.dadong;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 大东票务产品定义.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:35
 */
public class DadongProduct {

    public Long productId;

    public String category;

    public String productName;

    /**
     * 面值.
     */
    public BigDecimal faceValue;

    /**
     * 对外报价，售价.
     */
    public BigDecimal webValue;

    /**
     * 产品销售价，进价。
     */
    public BigDecimal platformValue;

    /**
     * 票面说明.
     */
    public String ticketExplain;

    /**
     * 资源方地址.
     */
    public String address;

    /**
     * 产品图片地址.
     */
    public String imageUrl;

    /**
     * 失效日期.
     */
    public Date expireTime;

    /**
     * 是否需要预订，对于需要预订的产品，需要在大东后台预订。
     */
    public Boolean isSubscribe;
}
