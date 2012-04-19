package controllers;

import java.io.File;
import java.util.Date;

import models.oauth.OauthToken;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.request.ItemImgUploadRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemImgUploadResponse;

import controllers.modules.resale.cas.SecureCAS;

@With(SecureCAS.class)
public class TopClient extends Controller{
    private static final String url = "http://gw.api.taobao.com/router/rest";
    private static final String appkey = "12576100";
    private static final String appsecret = "b1301dd48f91e483b42c914bbdf38d01";

    public static String ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    
    
    public static void add(Long favId){
        Resaler user = SecureCAS.getResaler();
        OauthToken token = getOauthToken(user.getId());
        ResalerFav resalerFav = ResalerFav.findById(favId);
        if(resalerFav == null){
            error("no fav found");
        }
        if(token == null || (token.accessTokenExpiresAt.getTime() - new Date().getTime()) <= 0){
            token.delete();
            redirect("http://container.api.taobao.com/container?appkey=12576100&encode=utf-8");
        }

        Goods goods = resalerFav.goods;
        TaobaoClient taobaoClient = new DefaultTaobaoClient(url, appkey, appsecret);
        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(goods.baseSale > 999999 ? 999999 : goods.baseSale);
        addRequest.setPrice(goods.getResalePrice(user.level).toString());
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(goods.name);
        addRequest.setDesc(goods.name);
        addRequest.setLocationState("上海");
        addRequest.setLocationCity("上海");
        addRequest.setCid(50022008L);
        
 
        ItemAddResponse response = addItem(taobaoClient, token.accessToken, goods.baseSale, goods.getResalePrice(user.level).toString(), goods.name, goods.name);
        
        if(response == null){
            renderArgs.put("errMsg", "请求淘宝服务出现异常，请稍后再试");
            render("ResalerFavs/topError.html");
            return;
        }
        
        if(response.getErrorCode() != null){
            renderArgs.put("errMsg", "请求淘宝服务出现异常：ErrorCode:" + response.getErrorCode() + 
                    "; ErrorMessage:" + response.getMsg() +
                    "; SubErrorCode:" + response.getSubCode() + 
                    "; SubErrorMessage:" + response.getSubMsg());
            render("ResalerFavs/topError.html");
        }else{
            uploadImg(taobaoClient, token.accessToken,response.getItem().getNumIid(), new File(ROOT_PATH, goods.imagePath));
            
            Long iid = response.getItem().getNumIid();
            resalerFav.taobaoItemId = iid;
            resalerFav.save();
            redirect("/library");
        }
    }
    
    private static ItemAddResponse addItem(TaobaoClient taobaoClient, String token, Long num, String price, String title, String desc){
        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(num > 999999 ? 999999 : num);
        addRequest.setPrice(price);
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(title);
        addRequest.setDesc(desc);
        addRequest.setLocationState("上海");
        addRequest.setLocationCity("上海");
        addRequest.setCid(50022008L);
        
        ItemAddResponse response;
        try {
            response = taobaoClient.execute(addRequest, token); //执行API请求并打印结果
            Logger.debug("ItemAddResponse.body:"+response.getBody());
            return response;
        }catch (ApiException e){
            Logger.error(e, "add item to taobao failed");
            return null;
        }
        
    }
    
    private static ItemImgUploadResponse uploadImg(TaobaoClient taobaoClient, String token,Long numIid, File imgFile){
        ItemImgUploadRequest imgUploadRequest = new ItemImgUploadRequest();
        imgUploadRequest.setNumIid(numIid);
        imgUploadRequest.setImage(new FileItem(imgFile));
        imgUploadRequest.setIsMajor(true);
        
        try {
            ItemImgUploadResponse response =  taobaoClient.execute(imgUploadRequest, token); //执行API请求并打印结果
            Logger.debug("ItemImgUploadResponse.body:" + response.getBody());
            return response;
        }catch (ApiException e){
            Logger.error(e, "upload img to taobao failed");
            return null;
        }
       
    }
    
    private static OauthToken getOauthToken(Long userId){
        OauthToken token = OauthToken.find("byUserId", userId).first();
        if(token != null && (token.accessTokenExpiresAt.getTime() - new Date().getTime()) <= 0){
            token.delete();
            return null;
        }
        return token;
        
    }
    
}
