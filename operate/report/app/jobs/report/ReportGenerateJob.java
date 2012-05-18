package jobs.report;

import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.DB;
import play.jobs.Job;
import play.jobs.On;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 定时生成统计报表数据.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 11:19 AM
 */
@On("*/10 * * * * ?")  //每天凌晨5点执行
public class ReportGenerateJob extends Job {
    @Override
    public void doJob() {
        Logger.info("=================Start ReportGenerateJob ...");

        String[] reportNames = Play.configuration.getProperty("job.report.names", "").split(",");

        for (String reportName : reportNames) {
            Logger.info("-----Start to append report[" + reportName + "].");
            String selectSql = Play.configuration.getProperty("job.report.select." + reportName);
            String deleteSql = Play.configuration.getProperty("job.report.delete." + reportName);
            String insertSql = Play.configuration.getProperty("job.report.insert." + reportName);
            Logger.debug("selectSql:" + selectSql);
            Logger.debug("deleteSql:" + deleteSql);
            Logger.debug("insertSql:" + insertSql);
            addReportData(selectSql, deleteSql, insertSql);
            Logger.info("-----Report[" + reportName + "] done.");
        }

        Logger.info("=================End of ReportGenerateJob ...");
    }

    private void addReportData(String selectSql, String deleteSql, String insertSql) {
        try {
            ResultSet rs = DB.executeQuery(selectSql);
            if (rs != null) {
                if (StringUtils.isNotBlank(deleteSql)) {
                    DB.execute(deleteSql);
                }

                String[] sqlParts = insertSql.split("\\?");
                Logger.debug("sqlParts.length:" + sqlParts.length);

                PreparedStatement pm = DB.getConnection().prepareStatement(insertSql);

                while (rs.next()) {
                    for (int i = 1; i < sqlParts.length; i++) {
                        pm.setObject(i, rs.getObject(i));
                    }
                    pm.executeUpdate();
                }
            }
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error("Execute SQL ERROR:" + e.getMessage(), e);
        }
    }
}
