package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.cms.CmsQuestion;
import models.cms.GoodsType;
import models.consumer.User;
import models.consumer.UserVote;
import models.mail.MailMessage;
import models.mail.MailUtil;
import models.sales.*;
import play.Play;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import play.test.Fixtures;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;


/**
 * Created with IntelliJ IDEA.
 * User: hejun
 * Date: 12-8-6
 * Time: 下午2:04
 * To change this template use File | Settings | File Templates.
 */
public class PointGoodsUserQuestion extends Controller {

    private static String DATE_FORMAT = "yyyy-MM-dd";
    private static String QUESTION_MAIL_RECEIVER = Play.configuration.getProperty("user_question.receiver");

    public static void add(String content, Long goodsId) throws ParseException{
        User user = SecureCAS.getUser();
        Http.Cookie cookie = request.cookies.get("identity");
        String cookieValue = cookie == null ? null : cookie.value;
        //System.out.println(">>>>>>>>>>>."+cookieValue);
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
        models.sales.PointGoods goods = null;
        if(goodsId != null){
            goods = models.sales.PointGoods.findById(goodsId);
        }
        if(goods == null){
            result.put("error", "该商品无法评论");
            renderJSON(result);
        }

        //保存并返回提问结果
        CmsQuestion question = new CmsQuestion();
        question.content = content;
        question.goodsId = goodsId;
        // save goods type of point goods
        question.goodsType = GoodsType.POINTGOODS;
        question.remoteIP = request.remoteAddress;
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

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        questionMap.put("date", dateFormat.format(question.createdAt));

        if (Play.runingInTestMode()){
            questionMap.put("date",dateFormat.format(dateFormat.parse("2012-07-26")));  //2012-07-26
        }

        List<Map<String, String>> questions = new ArrayList<>();
        questions.add(questionMap);
        result.put("questions", questions);

        Cache.delete(models.sales.PointGoods.CACHEKEY_BASEID + goodsId);

        //发送提醒邮件

        MailMessage mailMessage = new MailMessage();
        mailMessage.addRecipient(QUESTION_MAIL_RECEIVER);
        mailMessage.setSubject(Play.mode.isProd() ? "用户咨询" : "用户咨询【测试】");
        mailMessage.putParam("date", question.createdAt);
        mailMessage.putParam("user", questionMap.get("user"));
        mailMessage.putParam("content", question.content);
        mailMessage.putParam("goods", goods.name);
        MailUtil.sendOperatorNotificationMail(mailMessage);

        renderJSON(result);
    }

    public static void moreQuestions(Long goodsId, int firstResult, int size) throws ParseException{
        Http.Cookie idCookie = request.cookies.get("identity");
        String cookieValue = idCookie == null ? null : idCookie.value;
        Long userId = SecureCAS.getUser() == null ? null : SecureCAS.getUser().getId();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        List<CmsQuestion> questions = CmsQuestion.findOnGoodsShow(userId, cookieValue, goodsId, GoodsType.POINTGOODS, firstResult, size);
        List<Map<String, String>> mappedQuestions = new ArrayList<>();

        for (CmsQuestion question : questions){
            Map<String, String> mappedQuestion = new HashMap<>();
            mappedQuestion.put("content", question.content);
            mappedQuestion.put("date", dateFormat.format(question.createdAt));

            if (Play.runingInTestMode()){
                mappedQuestion.put("date", dateFormat.format(dateFormat.parse("2012-07-26")));
            }

            System.out.println(question.userName);

            if(question.userName != null){
                mappedQuestion.put("user", question.userName);
            }else {
                mappedQuestion.put("user", "游客");
            }

            mappedQuestions.add(mappedQuestion);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("questions", mappedQuestions);
        renderJSON(result);
    }
}
