package models.accounts;

import models.supplier.Supplier;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商户提现汇总报表
 * <p/>
 * User: wangjia
 * Date: 13-3-18
 * Time: 下午5:28
 */
public class SupplierWithdrawReport {

    public Supplier supplier;

    public BigDecimal unwithdrawedAmount;        //期初未提现金额

    public BigDecimal consumedAmount;            //本周期券消费金额

    public BigDecimal withdrawedAmount;          //本周期提现金额

    public BigDecimal remainedUnwithdrawedAmount;   //剩余未提现金额

    /*
        期初未提现金额
     */
    public SupplierWithdrawReport(Supplier supplier, BigDecimal unwithdrawedAmount) {
        this.supplier = supplier;
        this.unwithdrawedAmount = unwithdrawedAmount;
    }

    /*
        本周期券消费金额
     */
    public SupplierWithdrawReport(BigDecimal consumedAmount, Supplier supplier) {
        this.supplier = supplier;
        this.consumedAmount = consumedAmount;
    }


    /**
     * 取得商户提现汇总记录
     *
     * @param condition
     * @return
     */
    public static List<SupplierWithdrawReport> query(SupplierWithdrawCondition condition) {
        //期初未提现金额
        String sql = "select new models.SupplierWithdrawReport( " +
                " )" +
                " from  W";
        String groupBy = " group by r.goods.id";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilter() + groupBy + " order by sum(r.buyNumber) desc ");


        for (String param : condition.getParamMap().keySet()) {
            query.setParameter(param, condition.getParamMap().get(param));
        }

        List<SupplierWithdrawReport> unwithdrawedAmountList = query.getResultList();

    }
}
