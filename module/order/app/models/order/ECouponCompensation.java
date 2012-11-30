package models.order;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.Model;

/**
 * 分销渠道补偿验证记录。
 * 
 * 有时没有把消费记录同步到分销商，则通过这个功能进行。
 * @author tanglq
 *
 */
@Entity
@Table(name="ecoupon_compensations")
public class ECouponCompensation extends Model {

    public static final String CONSUMED = "CONSUMED";

    @ManyToOne
    public ECoupon ecpuon;
    
    /**
     * 补偿类型，包括： 消费确认CONSUMED, 退款REBATE
     */
    @Column(name="compensation_type")
    public String compensationType;
    
    @Column(name="compensated_at")
    public Date compensatedAt;
    
    public String result;
    
    /**
     * 得到需要处理的补偿。
     * @param type 补偿类型
     * @return
     */
    public static List<ECouponCompensation> findTodoCompensations(String type) {
        return ECouponCompensation.find("compensationType=? and compensatedAt is null", type).fetch();
    }
}
