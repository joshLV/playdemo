package models.accounts;

import models.supplier.Supplier;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/*
   期初未提现金额 = 采购成本-提现
*/


/**
 * 商户提现汇总报表
 * <p/>
 * User: wangjia
 * Date: 13-3-18
 * Time: 下午5:28
 */
public class SupplierWithdrawReport implements Serializable {

    public Supplier supplier;


    public BigDecimal unwithdrawedWithdrawAmount;        //提现

    public BigDecimal consumedAmount;            //本周期券消费金额

    public BigDecimal withdrawedAmount;          //本周期提现金额

    public BigDecimal remainedUnwithdrawedAmount;   //剩余未提现金额


    /**
     * 取得商户提现汇总记录
     *
     * @param condition
     * @return
     */
    public static List<SupplierWithdrawReport> query(SupplierWithdrawCondition condition) {
        //期初未提现金额
        String sql = "select a.account.uid,a.changeAmount,a.id" +
                " from AccountSequence a ";
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterUnwithdrawedPurchaseCostAmountAmount() + " order by createdAt desc");


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> unwithdrawedPurchaseCostAmountList = query.getResultList();
        System.out.println(unwithdrawedPurchaseCostAmountList.size() + "===unwithdrawedPurchaseCostAmountList.size()>>");
        Map<Long, SupplierWithdrawReport> supplierWithdrawResultMap = new HashMap<>();
        for (Object[] item : unwithdrawedPurchaseCostAmountList) {
            SupplierWithdrawReport tempSupplierWithdrawItem = new SupplierWithdrawReport();
            tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
            tempSupplierWithdrawItem.unwithdrawedWithdrawAmount = (BigDecimal) item[1];
            supplierWithdrawResultMap.put((Long) item[2], tempSupplierWithdrawItem);
        }

        List supplierWithdrawResultList = new ArrayList();
        for (Long key : supplierWithdrawResultMap.keySet()) {
            supplierWithdrawResultList.add(supplierWithdrawResultMap.get(key));
        }


        //本周期券消费金额
        System.out.println(supplierWithdrawResultList.size() + "===supplierWithdrawResultList>>");
        return supplierWithdrawResultList;

    }
}
