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

    public BigDecimal specificWithdrawnAmount;          //本周期提现金额

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
        Query query = JPA.em()
                .createQuery(sql + condition.getFilterPurchaseCost() + " order by createdAt desc");


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> purchaseCostList = query.getResultList();

        //merge purchaseCostList
        Map<Long, SupplierWithdrawReport> supplierWithdrawResultMap = new HashMap<>();
        SupplierWithdrawReport tempSupplierWithdrawItem;
        for (Object[] item : purchaseCostList) {
            tempSupplierWithdrawItem = new SupplierWithdrawReport();
            tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
            tempSupplierWithdrawItem.purchaseCost = (BigDecimal) item[1];
            tempSupplierWithdrawItem.previousUnwithdrawnAmount = tempSupplierWithdrawItem.purchaseCost;
            supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
        }

        //提现
        sql = "select a.account.uid,sum(a.changeAmount)" +
                " from AccountSequence a ";
        query = JPA.em()
                .createQuery(sql + condition.getFilterPreviousWithdrawnAmount() + " order by createdAt desc");


        for (String param : condition.getParams().keySet()) {
            query.setParameter(param, condition.getParams().get(param));
        }


        List<Object[]> previousWithdrawnAmountList = query.getResultList();

        //merge previousWithdrawnAmountList  期初未提现金额=采购成本-提现
        for (Object[] item : previousWithdrawnAmountList) {
            tempSupplierWithdrawItem = supplierWithdrawResultMap.get((Long) item[0]);
            System.out.println(tempSupplierWithdrawItem + "===tempSupplierWithdrawItem>>");
            if (tempSupplierWithdrawItem == null) {
                tempSupplierWithdrawItem = new SupplierWithdrawReport();
                tempSupplierWithdrawItem.supplier = Supplier.findById((Long) item[0]);
                tempSupplierWithdrawItem.previousWithdrawnAmount = (BigDecimal) item[1];
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = BigDecimal.ZERO.subtract(tempSupplierWithdrawItem.previousWithdrawnAmount);
                supplierWithdrawResultMap.put((Long) item[0], tempSupplierWithdrawItem);
            } else {
                tempSupplierWithdrawItem.previousWithdrawnAmount = (BigDecimal) item[1];
                tempSupplierWithdrawItem.previousUnwithdrawnAmount = tempSupplierWithdrawItem.purchaseCost.subtract(tempSupplierWithdrawItem.previousWithdrawnAmount);
            }
        }


        //本周期券消费金额




        //map-->list
        List supplierWithdrawResultList = new ArrayList();
        for (Long key : supplierWithdrawResultMap.keySet()) {
            supplierWithdrawResultList.add(supplierWithdrawResultMap.get(key));
        }

        return supplierWithdrawResultList;

    }
}
