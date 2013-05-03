package models.operator;

import com.uhuila.common.constants.DeletedStatus;
import play.data.validation.Match;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.view_ext.annotation.Mobile;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 运营商.
 * <p/>
 * User: wangjia
 * Date: 13-5-3
 * Time: 下午3:10
 */
@Entity
@Table(name = "operators")
public class Operator extends Model {
    @Required
    public String name;

    @Mobile
    @MinSize(value = 11)
    public String mobile;

    @Phone
    public String phone;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Required
    @Match(value = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", message = "邮箱格式不对！")
    public String email;

    @MaxSize(value = 500)
    public String remark;

    @Enumerated(EnumType.ORDINAL)
    public DeletedStatus deleted;


    @Column(name = "created_by")
    public String createdBy;

    @Column(name = "updated_by")
    public String updatedBy;

    public static String checkValue(Long id, String name) {
        StringBuilder sq = new StringBuilder("name = ? ");
        List list = new ArrayList();
        list.add(name);
        if (id != null) {
            sq.append("and id <> ?");
            list.add(id);
        }
        List<Operator> operatorList = Operator.find(sq.toString(), list.toArray()).fetch();
        String returnFlag = "0";
        //用户名存在的情况
        if (operatorList.size() > 0) returnFlag = "1";
        return returnFlag;
    }

}
