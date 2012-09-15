package models.dangdang;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * <p/>
 * User: yanjy
 * Date: 12-9-14
 * Time: 上午11:54
 */
public class ErrorInfo {
    @Enumerated(EnumType.STRING)
    public ErrorCode errorCode;
    public String errorDes;
    public String spid;
    public String ver;
}
