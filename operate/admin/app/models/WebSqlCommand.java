package models;

import com.uhuila.common.util.DateUtil;
import models.operator.OperateUser;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;
import play.modules.paginate.JPAExtPaginator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录sql命令执行历史.
 */
@Entity
@Table(name = "web_sql_commands")
public class WebSqlCommand extends Model {

    public String sql;

    @ManyToOne
    @JoinColumn(name = "supplier_user_id")
    public OperateUser operateUser;

    /**
     * 备注
     */
    public String remark;

    @Column(name = "executed_at")
    public Date executedAt;

    @Column(name = "result_count")
    public Integer resultCount;

    /**
     * 被引用次数. 记录重要程度.
     */
    @Column(name = "refer_count")
    public Integer referCount;

    /**
     * JSON格式的结果.
     */
    @Lob
    @Column(name = "sql_result")
    public String sqlResult;

    public static WebSqlCommand fromMessage(WebSqlCommandMessage message) {
        WebSqlCommand command = new WebSqlCommand();
        command.sql = message.sql;
        command.operateUser = OperateUser.findById(message.operateUserId);
        command.executedAt = message.executedAt;
        command.resultCount = message.resultCount;
        command.sqlResult = message.sqlResult;
        command.remark = message.remark;
        command.referCount = 0;
        return command;
    }

    public static JPAExtPaginator<WebSqlCommand> query(String text, Date beginAt, Date endAt, int pageNumber,
                                                           int pageSize) {
        StringBuffer sql = new StringBuffer("1=1");
        Map params = new HashMap();

        if (StringUtils.isNotBlank(text)) {
            sql.append(" and l.sql like :text");
            sql.append(" and l.operateUser.loginName like :text");
            sql.append(" and l.operateUser.userName like :text");
            sql.append(" and l.remark like :text");
            params.put("text", "%" + text + "%");
        }
        if (beginAt != null) {
            sql.append(" and l.executedAt >= :beginAt");
            params.put("beginAt", beginAt);
        }
        if (endAt != null) {
            sql.append(" and l.executedAt < :endAt");
            params.put("endAt", DateUtil.getEndOfDay(endAt));
        }

        JPAExtPaginator<WebSqlCommand> logPages = new JPAExtPaginator<>(
                "WebSqlCommand l", "l",
                WebSqlCommand.class, sql.toString(), params)
                .orderBy("l.id desc");
        logPages.setPageNumber(pageNumber);
        logPages.setPageSize(pageSize);
        logPages.setBoundaryControlsEnabled(true);
        return logPages;
    }
}
