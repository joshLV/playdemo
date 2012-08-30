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
import models.mail.MailMessage;
import models.mail.MailUtil;
import play.Play;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.With;
import controllers.modules.website.cas.SecureCAS;
import controllers.modules.website.cas.annotations.SkipCAS;

/**
 * @author likang
 */
@With({SecureCAS.class, WebsiteInjector.class})
@SkipCAS
public class UserQuestions extends Controller{
    private static String DATE_FORMAT = "yyyy-MM-dd";
    private static String QUESTION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static String[] QUESTION_MAIL_RECEIVERS = Play.configuration.getProperty("user_question.receivers","").split(",");

    public static void add(String content,String mobile, Long goodsId) throws ParseException{
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
        Map<String, String> questionMap = new HashMap<>();
        question.content = content;
        questionMap.put("content", content);
        if (mobile != null && mobile != ""){
            question.mobile = mobile;
            questionMap.put("mobile",mobile);
        }
        question.goodsId = goodsId;
        question.goodsName = goods.name;
        question.remoteIP = request.remoteAddress;
        if(user != null){
            question.userId = user.getId();
            question.userName = user.loginName;
            questionMap.put("user", user.loginName);
        }else {
            question.cookieId = cookieValue;
            questionMap.put("user", "游客");
        }
        question.save();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        
        questionMap.put("date", dateFormat.format(question.createdAt));
        
        if (Play.runingInTestMode()){
        	questionMap.put("date",dateFormat.format(dateFormat.parse("2012-07-26")));  //2012-07-26
        }
        
        List<Map<String, String>> questions = new ArrayList<>();
        questions.add(questionMap);
        result.put("questions", questions);
        
        Cache.delete(models.sales.Goods.CACHEKEY_BASEID + goodsId);

        //发送提醒邮件
        if (QUESTION_MAIL_RECEIVERS.length > 0 && !"".equals(QUESTION_MAIL_RECEIVERS[0])){
            MailMessage mailMessage = new MailMessage();
            mailMessage.addRecipient(QUESTION_MAIL_RECEIVERS);
            mailMessage.setSubject(Play.mode.isProd() ? "一百券用户咨询" : "一百券用户咨询【测试】");
            mailMessage.setFrom("一百券 <noreplay@uhuila.com>");
            mailMessage.putParam("date", new SimpleDateFormat(QUESTION_DATE_FORMAT).format(question.createdAt));
            mailMessage.putParam("user", questionMap.get("user"));
            mailMessage.putParam("content", question.content);
            mailMessage.putParam("mobile", question.mobile);
            mailMessage.putParam("goods", goods.name);
            mailMessage.putParam("questionId", question.id);
            mailMessage.putParam("goodsId", goods.id);
            mailMessage.setTemplate("userQuestion");
            MailUtil.sendOperatorNotificationMail(mailMessage);
        }
  
        renderJSON(result);
    }

    public static void moreQuestions(Long goodsId, int firstResult, int size) throws ParseException{	
        Http.Cookie idCookie = request.cookies.get("identity");
        String cookieValue = idCookie == null ? null : idCookie.value;
        Long userId = SecureCAS.getUser() == null ? null : SecureCAS.getUser().getId();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        List<CmsQuestion> questions = CmsQuestion.findOnGoodsShow(userId, cookieValue, goodsId, GoodsType.NORMALGOODS, firstResult, size);
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
