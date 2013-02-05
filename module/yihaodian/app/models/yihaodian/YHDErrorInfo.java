package models.yihaodian;

/**
 * @author likang
 *         Date: 12-9-13
 */
public class YHDErrorInfo {
    public String errorCode;
    public String errorDes;
    public String pkInfo;

    public YHDErrorInfo(String errorCode, String errorDes, String pkInfo){
        this.errorCode = errorCode;
        this.errorDes = errorDes;
        this.pkInfo = pkInfo;
    }
}
