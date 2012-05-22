package jobs.report;

import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.db.DB;
import play.jobs.Job;
import play.jobs.On;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时生成统计报表数据.
 * <p/>
 * User: sujie
 * Date: 5/17/12
 * Time: 11:19 AM
 */
@On("0 0 5 * * ?")  //每天凌晨5点执行
public class ReportGenerateJob extends Job {
    @Override
    public void doJob() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-HH-dd");

        final String today = format.format(new Date());
        Logger.info("=================Start ReportGenerateJob(" + today + ") ...");

        String[] reportNames = Play.configuration.getProperty("job.report.names", "").split(",");

        for (String reportName : reportNames) {
            Logger.info("--Start to append report[" + reportName + "].");

            long start = System.currentTimeMillis();

            String selectSql = Play.configuration.getProperty("job.report.select." + reportName);
            String deleteSql = Play.configuration.getProperty("job.report.delete." + reportName);
            String insertSql = Play.configuration.getProperty("job.report.insert." + reportName);

            Logger.debug("selectSql:" + selectSql);
            Logger.debug("deleteSql:" + deleteSql);
            Logger.debug("insertSql:" + insertSql);

            addReportData(selectSql, deleteSql, insertSql);

            long end = System.currentTimeMillis();
            BigDecimal timeConsuming = new BigDecimal(end - start).divide(new BigDecimal(1000));
            Logger.info("----------Report[" + reportName + "] done(" + timeConsuming.toString() + "sec).");
        }

        Logger.info("=================End of ReportGenerateJob(" + today + ") ...");
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
                    int result = pm.executeUpdate();
                    System.out.println("insert result:" + result);
                }
            }
            DB.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error("Execute SQL ERROR:" + e.getMessage(), e);
        }
    }
}
