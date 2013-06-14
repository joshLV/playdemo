package models;

import models.mq.QueueIDMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * User: tanglq
 * Date: 13-3-6
 * Time: 下午1:36
 */
public class WebSqlCommandMessage extends QueueIDMessage implements Serializable {

    private static final long serialVersionUID = 798167831652L;

    public static final String MQ_KEY = "admin.websql.cmd";

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

    @Override
    public String messageId() {
        return MQ_KEY + this.hashCode();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(this.sql).append(this.operateUserId).append(this.remark).append(this.sqlType).append(this.executedAt).append(this.resultCount).append(this.sqlResult).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WebSqlCommandMessage other = (WebSqlCommandMessage) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.sql, other.sql).append(this.operateUserId, other.operateUserId).append(this.remark, other.remark).append(this.sqlType, other.sqlType).append(this.executedAt, other.executedAt).append(this.resultCount, other.resultCount).append(this.sqlResult, other.sqlResult).isEquals();
    }
}
