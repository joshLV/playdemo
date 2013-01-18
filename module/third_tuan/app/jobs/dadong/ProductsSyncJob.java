package jobs.dadong;

import play.Play;
import play.jobs.Job;
import play.templates.Template;
import play.templates.TemplateLoader;
import util.ws.WebServiceClient;
import util.ws.WebServiceClientFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步大东商品，返回同步商品数。
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:24
 */
public class ProductsSyncJob extends Job<Integer> {
    @Override
    public Integer doJobWithResult() throws Exception {

        String origanCode = Play.configuration.getProperty("dadong.origin.code", "shanghaishihui_201301145784");
        String url = Play.configuration.getProperty("dadong.url", "http://www.ddrtty.net/bjskiService.action");

        Template template = TemplateLoader.load("xml/template/dadong/GetProductRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("origan_code", origanCode);
        int pageIndex = 0;

        do {
            args.put("page_index", pageIndex);
            String xml = template.render(args);
            Map<String, String> params = new HashMap<>();
            WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

            client.postXml("thirdtuan.dadang.GetProducts", url, params, origanCode);

        } while (pageIndex >= 100);


        return 0;
    }
}
