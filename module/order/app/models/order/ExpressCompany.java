package models.order;

import cache.CacheHelper;
import play.data.validation.Required;
import play.db.jpa.JPA;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String CACHEKEY = "EXPRESS_COMPANY";

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
        updatedExpress.refresh();
        updatedExpress.code = express.code;
        updatedExpress.name = express.name;
        updatedExpress.resalerMapping = express.resalerMapping;
        updatedExpress.save();
    }

    public static Map<String, String> findChannelExpress(Long expressId) {
        ExpressCompany express = ExpressCompany.findById(expressId);
        Map<String, String> channelMap = new HashMap<>();
        System.out.println(express.resalerMapping + "===express.resalerMapping>>");
        String[] line = express.resalerMapping.split("\r\n");
        for (int i = 0; i < line.length; i++) {
            System.out.println(line[i] + "===line[i]>>");
            System.out.println("");
            if (line[i].contains(":")) {
                System.out.println(  "===>>");
                String[] channelMapping = line[i].split(":");
                channelMap.put(channelMapping[0], channelMapping[1]);
            }
        }
        return channelMap;
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

    @Override
    public void _save() {
        CacheHelper.delete(CACHEKEY);
//        CacheHelper.delete(CACHEKEY + this.id);
        super._save();
    }


}
