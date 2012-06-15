package controllers;

import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;
import models.cms.CmsQuestion;
import models.consumer.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author likang
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class UserQuestions extends Controller{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static void add(String content, Long goodsId){
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;

        Map<String,Object> result = new HashMap<>();
        // 无法鉴定身份
        if (user == null && cookieValue == null){
            result.put("error", "无法获知提问者身份");
            renderJSON(result);
        }

        //问题无效
        if(content == null || "".equals(content.trim())){
            result.put("error", "请输入问题");
            renderJSON(result);
        }
        //检查对哪个商品提问
        models.sales.Goods goods = null;
        if(goodsId != null){
            goods = models.sales.Goods.findById(goodsId);
        }
        if(goods == null){
            result.put("error", "该商品无法评论");
            renderJSON(result);
        }

        //保存并返回提问结果
        CmsQuestion question = new CmsQuestion();
        question.content = content;
        question.goodsId = goodsId;
        Map<String, String> questionMap = new HashMap<>();
        if(user != null){
            question.userId = user.getId();
            question.userName = user.loginName;
            questionMap.put("user", user.loginName);
        }else {
            question.cookieId = cookieValue;
            questionMap.put("user", "游客");
        }
        question.save();

        questionMap.put("content", content);
        questionMap.put("date", dateFormat.format(question.createdAt));
        List<Map<String, String>> questions = new ArrayList<>();
        questions.add(questionMap);
        result.put("questions", questions);
        renderJSON(result);
    }

}
