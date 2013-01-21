package jobs.dadong;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
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
 *
 * 使用Job作为异步请求机制.
 * User: tanglq
 * Date: 13-1-18
 * Time: 下午5:24
 */
public class DadongProductsSyncJob extends Job<Integer> {
    @Override
    public Integer doJobWithResult() throws Exception {

        String origanCode = Play.configuration.getProperty("dadong.origin.code", "shanghaishihui_201301145784");
        String url = Play.configuration.getProperty("dadong.url", "http://www.ddrtty.net/bjskiService.action");

        Template template = TemplateLoader.load("app/xml/template/dadong/GetProductRequest.xml");
        Map<String, Object> args = new HashMap<>();
        args.put("origan_code", origanCode);
        int pageIndex = 0;
        boolean nextLoop = true;
        do {
            args.put("page_index", pageIndex);
            String xml = template.render(args);
            Map<String, Object> params = new HashMap<>();
            params.put("xml", xml);
            WebServiceClient client = WebServiceClientFactory.getClientHelper("GB2312");

            System.out.println("get result : " + pageIndex);
            try {
                Document document = client.postXml("thirdtuan.dadang.GetProducts", url, params, String.valueOf(pageIndex));
                NodeList products = document.getElementsByTagName("products");
                if (products == null || products.getLength() == 0) {
                    Logger.info("找不到products");
                    break;
                }
                for (int i = 0; i < products.getLength(); i++) {
                    System.out.println(i + " get products.....");
                    Node node = products.item(0);
                }
            } catch (Exception e) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>");
                e.printStackTrace();
                nextLoop = false;
            }
            pageIndex ++;

        } while (nextLoop);


        return 1;
    }
}
