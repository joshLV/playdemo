package models.sales;

import com.uhuila.common.constants.DeletedStatus;
import com.uhuila.common.util.DateUtil;
import models.order.ECouponStatus;
import org.apache.commons.lang.StringUtils;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-4
 * Time: 上午10:33
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "telephone_message")
public class TelephoneMessage extends Model {


    /**
     * 创建时间
     */
    @Column(name = "created_at")
    public Date createdAt;
    /**
     * 创建人
     */
    @Column(name = "created_by")
    public String createdBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    public Date updatedAt;

    /**
     * 咨询类型
     */
    @Column(name = "consult_type")
    @Enumerated(EnumType.STRING)
    public ConsultType consultType;

    /**
     * 内容
     */
    @Required
    @MaxSize(65000)
    @Lob
    public String text;


    public String phone;

    /**
     * 逻辑删除,0:未删除，1:已删除
     */
    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;


//    public static void delete(Long... ids) {
//        for (Long id : ids) {
//            models.sales.TelephoneMessage consult = models.sales.TelephoneMessage.findById(id);
//            if (consult != null) {
//                consult.deleted = DeletedStatus.DELETED;
//                consult.save();
//            }
//        }
//    }


    public static void delete(Long id) {

        models.sales.TelephoneMessage consult = models.sales.TelephoneMessage.findById(id);
        if (consult != null) {
            consult.deleted = DeletedStatus.DELETED;
            consult.save();
        }

    }


    public static void update(Long id, TelephoneMessage consult) {
        TelephoneMessage updateConsult = TelephoneMessage.findById(id);
        if (updateConsult == null) {
            return;
        }
        updateConsult.updatedAt=new Date();
        updateConsult.consultType = consult.consultType;
        updateConsult.text = consult.text;

        updateConsult.save();
    }

    //=================================================== 数据库操作 ====================================================

    @Override
    public boolean create() {

        createdAt = new Date();

        return super.create();
    }
}
