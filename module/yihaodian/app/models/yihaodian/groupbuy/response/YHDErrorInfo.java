package models.yihaodian.groupbuy.response;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class YHDErrorInfo {
    private String errorCode;
    private String errorDes;
    private String pkInfo;
    public YHDErrorInfo(String errorCode, String errorDes, String pkInfo){
        this.errorCode = errorCode;
        this.errorDes = errorDes;
        this.pkInfo = pkInfo;
    }
}
