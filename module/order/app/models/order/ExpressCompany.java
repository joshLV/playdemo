package models.order;

import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
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
    public String resalerMapping;


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
