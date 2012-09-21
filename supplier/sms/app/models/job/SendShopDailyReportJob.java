package models.job;

import com.uhuila.common.constants.DeletedStatus;
import models.sales.Shop;
import models.sms.SMSUtil;
import models.supplier.Supplier;
import models.supplier.SupplierStatus;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.db.DB;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author likang
 * Date: 12-7-30
 */
@On("0 0 9 * * ?")
public class SendShopDailyReportJob extends Job{
    private static final String MOBILE_PATTERN = "^1\\d{10}$";
    @Override
    public void doJob(){
        String sql = "select shop_id, count(*), sum(face_value) " +
                "from e_coupon where shop_id is not null and consumed_at is not null " +
                "and consumed_at >= CURRENT_DATE-INTERVAL 1 DAY " +
                "and consumed_at < CURRENT_DATE " +
                "group by shop_id";
        try{
            ResultSet rs = DB.executeQuery(sql);
            if (rs != null) {
                while (rs.next()) {
                    Long shopId = rs.getLong(1);
                    int totalCount = rs.getInt(2);
                    BigDecimal totalValue = rs.getBigDecimal(3);
                    sendReport(shopId, totalCount, totalValue);
                }
            }
            DB.close();
        } catch (SQLException e) {
            Logger.error("send shop daily report job error: ", e);
        }

    }

    private void sendReport(Long shopId, int totalCount, BigDecimal totalValue){
        if(totalCount == 0) {
            return;
        }
        Shop shop = Shop.findById(shopId);
        if(shop == null) {
            Logger.error("send shop daily report error. can not find shop: " + shopId);
            return;
        }
        Supplier supplier = Supplier.findById(shop.supplierId);
        if (supplier == null || supplier.status != SupplierStatus.NORMAL || supplier.deleted == DeletedStatus.DELETED) {
            Logger.error("send shop daily report error. Invalid supplier: " + shop.supplierId);
            return;
        }
        if (shop.managerMobiles == null || shop.managerMobiles.trim().length() == 0){
            return;
        }
        String mobiles[] = shop.managerMobiles.split(",");
        for (String mobile : mobiles) {
            if(Pattern.compile(MOBILE_PATTERN).matcher(mobile).matches()){
                SMSUtil.send("", mobile);
            }
        }
    }
}
