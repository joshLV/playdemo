package models;

import java.io.Serializable;
import java.util.Date;

/**
 * User: tanglq
 * Date: 13-3-6
 * Time: 下午1:36
 */
public class WebSqlCommandMessage implements Serializable {

    private static final long serialVersionUID = 70632388310652L;

    public static final String MQ_KEY = "admin.websql.commmands";

    public String sql;

    public Long operateUserId;

    public String remark;

    public String sqlType;

    public Date executedAt;

    public Integer resultCount;

    /**
     * JSON格式的结果.
     */
    public String sqlResult;

}
