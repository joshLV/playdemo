package controllers;

import com.google.gson.Gson;
import com.uhuila.common.util.DateUtil;
import models.WebSqlCommand;
import models.WebSqlCommandMessage;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.DB;
import play.modules.paginate.JPAExtPaginator;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import util.mq.MQPublisher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL命令执行器.
 */
@With(OperateRbac.class)
@ActiveNavigation("web_sql_executor")
public class WebSqlExcutors extends Controller {

    public static final String SESSION_SECRET_KEY = "sqlExecutor.secret";
    public static final String SECRET_KEY = Play.configuration.getProperty("sqlExecutor.secret", "");
    public static final String DB_URL = Play.configuration.getProperty("db");

    @Before(unless = "secret")
    public static void checkSecret() {
        if (!StringUtils.isBlank(SECRET_KEY)) {
            if (!SECRET_KEY.equals(session.get(SESSION_SECRET_KEY))) {
                // 显示安全码输入页面
                secret(null);
            }
        }
    }

    /**
     * 默认页面，显示sql输入窗口.
     */
    public static void index(Long id) {
        if (id != null) {
            WebSqlCommand sqlCommand = WebSqlCommand.findById(id);
            sqlCommand.referCount += 1;
            sqlCommand.save();
            String sql = sqlCommand.sql;
            String remark = sqlCommand.remark;
            render(sql, remark);
        }
        render();
    }

    /**
     * 执行sql
     *
     * @param sql
     * @param remark
     */
    public static void run(String sql, String remark) {

        Logger.info("sql:" + sql + ", remark:" + remark);
        List<WebSqlCommandMessage> commands = splitSql(sql, remark);

        if (commands == null) {
            String message = "无法识别的SQL";
            Logger.info("message=" + message);
            render("WebSqlExcutors/index.html", sql, remark, message);
        }

        // 只能先执行一条sql
        WebSqlCommandMessage command = commands.get(0);
        renderArgs.put("sql", command.sql);
        renderArgs.put("remark", command.remark);

        Connection conn = null;
        try {
            conn = DB.getConnection();


            Statement stmt = conn.createStatement();
            stmt.executeUpdate("SET SQL_SAFE_UPDATES=1"); //打开安全模式

            List<String> columnNames = new ArrayList<>();
            List<Map<String, Object>> resultMaps = new ArrayList<>();
            if ("SELECT".equals(command.sqlType)) {
                Logger.info("command.sql=" + command.sql);
                ResultSet rs = stmt.executeQuery(command.sql);
                ResultSetMetaData rsMeta = rs.getMetaData();

                command.executedAt = new Date();

                rs.last();
                command.resultCount = rs.getRow();
                rs.beforeFirst();

                for (int i = 0; i < rsMeta.getColumnCount(); ++i) {
                    String columnName = rsMeta.getColumnName(i + 1);
                    columnNames.add(columnName);
                }
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    for (int i = 0; i < columnNames.size(); i++) {
                        String columnName = columnNames.get(i);
                        Object value = rs.getObject(i + 1);
                        row.put(columnName, value);
                    }
                    resultMaps.add(row);
                    if (resultMaps.size() >= 100) {
                        break;  //最多只返回100行记录，以避免导库
                    }
                }
                renderArgs.put("columnNames", columnNames);
                renderArgs.put("resultMaps", resultMaps);
                command.sqlResult = new Gson().toJson(resultMaps);
            } else {
                int resultCount = stmt.executeUpdate(command.sql);
                command.executedAt = new Date();
                command.sqlResult = "{\"" + command.sqlType + "Count\":" + resultCount + "}";
                command.resultCount = resultCount;
                String message = "执行" + command.sqlType + "操作，影响" + resultCount + "条记录。";
                renderArgs.put("message", message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            renderArgs.put("message", e.getMessage());
        } finally {
            MQPublisher.publish(WebSqlCommandMessage.MQ_KEY, command);
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        render("WebSqlExcutors/index.html");
    }

    private static List<WebSqlCommandMessage> splitSql(final String inputSQL, String remark) {
        String[] sqlAry = inputSQL.split(";");

        List<WebSqlCommandMessage> commands = new ArrayList<>();

        for (String sql : sqlAry) {
            sql = sql.trim();
            if (StringUtils.isNotBlank(sql)) {
                WebSqlCommandMessage command = new WebSqlCommandMessage();
                String upcaseSQL = sql.toUpperCase();
                command.sql = sql;
                command.sqlType = getSqlType(upcaseSQL);
                if (command.sqlType == null) {
                    return null;  //不可识别的SQL
                }
                command.remark = remark;
                command.operateUserId = OperateRbac.currentUser().id;
                commands.add(command);
            }
        }

        return commands;
    }


    private static String getSqlType(String upcaseSQL) {
        if (upcaseSQL.startsWith("SELECT")) {
            return "SELECT";
        }
        if (upcaseSQL.startsWith("INSERT")) {
            return "INSERT";
        }
        if (upcaseSQL.startsWith("UPDATE")) {
            return "UPDATE";
        }
        if (upcaseSQL.startsWith("DELETE")) {
            return "DELETE";
        }
        return null;
    }


    public static void history(String text, Date beginAt, Date endAt) {

        if (beginAt == null) {
            beginAt = DateUtil.getBeginOfDay();
        }
        if (endAt == null) {
            endAt = DateUtil.getEndOfDay(new Date());
        }

        String page = request.params.get("page");
        int pageNumber = StringUtils.isEmpty(page) ? 1 : Integer.parseInt(page);

        JPAExtPaginator<WebSqlCommand> sqlPage = WebSqlCommand.query(text, beginAt, endAt,
                pageNumber, 30);


        render(text, beginAt, endAt, sqlPage);
    }

    /**
     * 检查安全码.
     *
     * @param secret
     */
    public static void secret(String secret) {
        if (!SECRET_KEY.equals(secret)) {
            // 显示安全码输入页面
            render();
        }
        session.put(SESSION_SECRET_KEY, SECRET_KEY);
        index(null);
    }
}
