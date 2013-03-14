package models.order;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * 快递公司
 * <p/>
 * User: wangjia
 * Date: 13-3-13
 * Time: 上午10:44
 */

@Entity
@Table(name = "express_company")
public class ExpressCompany extends Model {

    @Required
    public String code;

    @Required
    public String name;

    /*
      渠道方与对应的码的映射关系  eg:JD：63
    */
    @Column(name = "resaler_mapping")
    @Required
    @Lob
    public String resalerMapping;

    public static void update(Long id, ExpressCompany express) {
        ExpressCompany updatedExpress = findById(id);
        updatedExpress.code = express.code;
        updatedExpress.name = express.name;
        updatedExpress.resalerMapping = express.resalerMapping;
        updatedExpress.save();
    }

    /**
     * 取得快递公司名称
     *
     * @param code
     * @return
     */
    public static ExpressCompany getCompanyNameByCode(String code) {
        return ExpressCompany.find("code=?", code).first();
    }
}
