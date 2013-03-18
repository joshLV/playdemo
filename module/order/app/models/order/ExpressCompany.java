package models.order;

import cache.CacheCallBack;
import cache.CacheHelper;
import org.apache.commons.lang.StringUtils;
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

    public static Map<String, String> findChannelExpress() {
        return CacheHelper.getCache(CacheHelper.getCacheKey(models.order.ExpressCompany.CACHEKEY, "CHANNEL_MAPPING"), new CacheCallBack<Map<String, String>>() {
            @Override
            public Map<String, String> loadData() {
                List<ExpressCompany> expressList = ExpressCompany.findAll();
                Map<String, String> channelMap = new HashMap<>();
                for (ExpressCompany express : expressList) {
                    String[] line = express.resalerMapping.split("\n");
                    for (int i = 0; i < line.length; i++) {
                        if (line[i].contains(":")) {
                            String[] channelMapping = line[i].split(":");
                            if (StringUtils.isNotBlank(channelMapping[0]) && StringUtils.isNotBlank(channelMapping[1])) {
                                channelMap.put(channelMapping[0].trim() + "_" + express.id, channelMapping[1].trim());
                            }
                        }
                    }
                }
                return channelMap;
            }
        });
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
