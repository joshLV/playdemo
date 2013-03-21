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

    public BigDecimal purchaseCost;   //采购成本

    public BigDecimal previousWithdrawnAmount;        //提现

    public BigDecimal previousUnwithdrawnAmount;  //期初未提现金额(采购成本-提现)

    public BigDecimal consumedAmount;            //本周期券消费金额

    public BigDecimal withdrawnAmount;          //本周期提现金额

    public BigDecimal remainedUnwithdrawnAmount;   //剩余未提现金额


    /**
     * 取得商户提现汇总记录
     *
     * @param condition
     * @return
     */
    public static List<SupplierWithdrawReport> query(SupplierWithdrawCondition condition) {
        /*
           期初未提现金额
        */

        //采购成本
        String sql = "select a.account.uid,sum(a.changeAmount)" +
                " from AccountSequence a ";
        String groupBy = " group by a.account.uid";

        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPurchaseCost() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> purchaseCostList = query.getResultList();
        System.out.println(purchaseCostList.size() + "===purchaseCostList.size()>>");
        //merge purchaseCostList
        Map<Long, SupplierWithdrawReport> supplierWithdrawResultMap = new HashMap<>();
        SupplierWithdrawReport tempSupplierWithdrawItem;
        for (Object[] item : purchaseCostList) {
            tempSupplierWithdrawItem = new SupplierWithdrawReport();
//            System.out.println((Long)item[0] + "===(Long)item[0]>>");
            tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
            tempSupplierWithdrawItem.purchaseCost = (BigDecimal) item[1];
            tempSupplierWithdrawItem.previousUnwithdrawnAmount = tempSupplierWithdrawItem.purchaseCost;
            tempSupplierWithdrawItem.consumedAmount = BigDecimal.ZERO;
            tempSupplierWithdrawItem.withdrawnAmount = BigDecimal.ZERO;
            tempSupplierWithdrawItem.remainedUnwithdrawnAmount = BigDecimal.ZERO;
            supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
        }

        //提现
        sql = "select a.account.uid,sum(a.changeAmount)" +
                " from AccountSequence a ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterPreviousWithdrawnAmount() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> previousWithdrawnAmountList = query.getResultList();

        //merge previousWithdrawnAmountList  期初未提现金额=采购成本-提现
        for (Object[] item : previousWithdrawnAmountList) {
            tempSupplierWithdrawItem = supplierWithdrawResultMap.get((Long) item[0]);
            if (tempSupplierWithdrawItem == null) {
                tempSupplierWithdrawItem = new SupplierWithdrawReport();
                tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
                tempSupplierWithdrawItem.previousWithdrawnAmount = (BigDecimal) item[1];
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = BigDecimal.ZERO.subtract(tempSupplierWithdrawItem.previousWithdrawnAmount);
                tempSupplierWithdrawItem.consumedAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.withdrawnAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.remainedUnwithdrawnAmount = BigDecimal.ZERO;
                supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
            } else {
                tempSupplierWithdrawItem.previousWithdrawnAmount = (BigDecimal) item[1];
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = tempSupplierWithdrawItem.purchaseCost.subtract(tempSupplierWithdrawItem.previousWithdrawnAmount);
            }
        }


        //本周期券消费金额
        sql = "select a.account.uid,sum(a.changeAmount)" +
                " from AccountSequence a ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterConsumedAmount() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> consumedAmountList = query.getResultList();

        //merge consumedAmountList
        for (Object[] item : consumedAmountList) {
            tempSupplierWithdrawItem = supplierWithdrawResultMap.get((Long) item[0]);
            if (tempSupplierWithdrawItem == null) {
                tempSupplierWithdrawItem = new SupplierWithdrawReport();
                tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.consumedAmount = (BigDecimal) item[1];
                tempSupplierWithdrawItem.withdrawnAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.remainedUnwithdrawnAmount = BigDecimal.ZERO;
                supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
            } else {
                tempSupplierWithdrawItem.consumedAmount = (BigDecimal) item[1];
            }
        }

        //本周期提现金额
        sql = "select a.account.uid,sum(a.changeAmount)" +
                " from AccountSequence a ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterWithdrawnAmount() + groupBy);


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> withdrawnAmountList = query.getResultList();

        //merge withdrawnAmountList
        for (Object[] item : withdrawnAmountList) {
            tempSupplierWithdrawItem = supplierWithdrawResultMap.get((Long) item[0]);
            if (tempSupplierWithdrawItem == null) {
                tempSupplierWithdrawItem = new SupplierWithdrawReport();
                tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.consumedAmount = BigDecimal.ZERO;
                tempSupplierWithdrawItem.withdrawnAmount = BigDecimal.ZERO.subtract((BigDecimal) item[1]);
                tempSupplierWithdrawItem.remainedUnwithdrawnAmount = BigDecimal.ZERO;
                supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
            } else {
                tempSupplierWithdrawItem.withdrawnAmount = BigDecimal.ZERO.subtract((BigDecimal) item[1]);
            }
        }


        //map-->list
        List supplierWithdrawResultList = new ArrayList();
        for (Long key : supplierWithdrawResultMap.keySet()) {
            supplierWithdrawResultList.add(supplierWithdrawResultMap.get(key));
        }

        return supplierWithdrawResultList;

    }
}
