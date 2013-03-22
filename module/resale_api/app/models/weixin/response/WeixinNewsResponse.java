package models.weixin.response;

import models.weixin.WeixinMessageType;
import models.weixin.WeixinResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author likang
 *         Date: 13-3-19
 */
public class WeixinNewsResponse extends WeixinResponse {
    public List<Article> articles = new ArrayList<>();

    public void addArticle(String title, String description, String picUrl, String url) {
        articles.add(new Article(title, description, picUrl, url));
    }

    @Override
    public WeixinMessageType getMsgType() {
        return WeixinMessageType.NEWS;
    }

    class Article{
        public String title;
        public String description;
        public String picUrl;
        public String url;

        public Article(String title, String description, String picUrl, String url) {
            this.title = title;
            this.description = description;
            this.picUrl = picUrl;
            this.url = url;
        }
    }
}
