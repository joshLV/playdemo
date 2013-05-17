package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.db.DB;
import play.mvc.Controller;
import play.mvc.Http;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 *         Date: 13-5-17
 */
public class YaboWizard extends Controller {
    //to get another one, please open this url: http://api.wordpress.org/secret-key/1.1/
    private static final String KEY = "JwPslo.-l[|k*3{)__V]i{^[_;A0@GV,Ma7Gf0`qa4P@ks=p9qN?RM0*y}Xxf*DK";
    private static final String on = Play.configuration.getProperty("yabo.wizard","off");

    public static void wizard(String sql, String key) {
        if (!on.equals("on")) {
            response.status = Http.StatusCode.FORBIDDEN;
            renderJSON("{" + "\"error\": \"打烊了\"" + "}");
        }

        if (StringUtils.isBlank(sql) || StringUtils.isBlank(key)) {
            response.status = Http.StatusCode.BAD_REQUEST;
            renderJSON("{" + "\"error\": \"无效的参数\"" + "}");
        }
        if (!key.equals(KEY)) {
            response.status = Http.StatusCode.UNAUTHORIZED;
            renderJSON("{" + "\"error\": \"请你闪开\"" + "}");
        }

        Connection conn = DB.getConnection();
        List<String> columnNames = new ArrayList<>();
        int totalCount = 0;
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("SET SQL_SAFE_UPDATES=1"); //mysql要打开安全模式

            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnNames.add(columnName);
            }

            rs.last();
            totalCount = rs.getRow();
            rs.beforeFirst();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 0; i < columnNames.size(); i++) {
                    String columnName = columnNames.get(i);
                    Object value = rs.getObject(i + 1);
                    row.put(columnName, value);
                }

                rows.add(row);
                if (rows.size() >= 200) {
                    break;  //最多只返回200行记录，以避免导库
                }
            }

        } catch (SQLException e) {
            response.status = Http.StatusCode.INTERNAL_ERROR;
            renderJSON("{" + "\"error\": \"无效的参数\"" + "}");
            return;
        }
        result.put("totalCount", totalCount);
        result.put("columnNames", columnNames);
        result.put("rows", rows);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        renderJSON(gson.toJson(result));
    }
}
