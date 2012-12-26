package models.wuba;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * <p/>
 * User: yanjy
 * Date: 12-12-25
 * Time: 上午11:15
 */
@Table(name = "wuba_area_mapping")
@Entity
public class AreaMapping extends Model {
    @Column(name = "ybq_circle_name")
    public String ybqCircleName;
    @Column(name = "wb_circle_name")
    public String wbCircleName;

    @Column(name = "created_at")
    public Date createdAt;

    public AreaMapping( String ybqCircleName, String wbCircleName) {
        this.wbCircleName = wbCircleName;
        this.ybqCircleName = ybqCircleName;
        this.createdAt = new Date();
    }

    public static AreaMapping getArea(String ybqCircleName) {
        return AreaMapping.find("ybqCircleName=? order by id desc", ybqCircleName).first();
    }
}
