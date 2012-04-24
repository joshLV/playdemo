package controllers;

import java.io.File;
import java.util.Date;
import java.util.List;

import models.oauth.OauthToken;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.Shop;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;
import java.math.BigDecimal;

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

    public static String IMG_ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String TAOBAO_TOP_OAUTH_URL = Play.configuration.getProperty("taobao.top.oauth_url","http://container.api.taobao.com/container?appkey=12576100&encode=utf-8");
    
    public static void add(Long goodsId){
        Resaler user = SecureCAS.getResaler();
        OauthToken token = getOauthToken(user.getId());
        Goods goods = Goods.findById(goodsId);
        ResalerFav resalerFav = ResalerFav.find("byGoods",goods).first();
        if(resalerFav == null){
            error("no fav found");
        }
        if(token == null){
            redirect(TAOBAO_TOP_OAUTH_URL);
        }
        
        ItemAddResponse response = uploadGoods(resalerFav.goods, token, user,null ,null);
        if(response == null){
            renderArgs.put("errMsg", "");
            render("ResalerFavs/topError.html");
        }else if(response.getErrorCode() != null){
            renderArgs.put("errMsg", "请求淘宝服务出现异常：ErrorCode:" + response.getErrorCode() + 
                    "; ErrorMessage:" + response.getMsg() +
                    "; SubErrorCode:" + response.getSubCode() + 
                    "; SubErrorMessage:" + response.getSubMsg());
            render("ResalerFavs/topError.html");
        }else{
            Long iid = response.getItem().getNumIid();
            resalerFav.taobaoItemId = iid;
            resalerFav.save();
            redirect("/library");
        }
    }
    
    /**
     * 
     * 
     * @param favIds
     */
    public static void batchAdd(@As(",") List<Long> goodsIds, int pricemakupRate, String pricemakup){
        Resaler user = SecureCAS.getResaler();
        OauthToken token = getOauthToken(user.getId());
        if(token == null){
            redirect(TAOBAO_TOP_OAUTH_URL);
        }
        for(Long goodsId : goodsIds){
            Goods goods = Goods.findById(goodsId);
            ResalerFav resalerFav = ResalerFav.find("byGoods", goods).first();
            if(resalerFav == null ){
                continue;
            }
            ItemAddResponse response =  uploadGoods(resalerFav.goods, token, user, String.valueOf(pricemakupRate/100.0), pricemakup);
            if(response != null && response.getErrorCode() == null){
                Long iid = response.getItem().getNumIid();
                resalerFav.taobaoItemId = iid;
                resalerFav.save();
            }
        }
        redirect("/library");
    }
    
    /**
     * 上传商品到淘宝
     * 
     * @param goods 商品
     * @param token OAuth token
     * @param resaler 分销商
     * @return 上传结果
     */
    private static ItemAddResponse uploadGoods(Goods goods, OauthToken token, Resaler resaler, String pricemakupRate, String pricemakup){
        TaobaoClient taobaoClient = new DefaultTaobaoClient(url, appkey, appsecret);
        
        StringBuilder desc = new StringBuilder(goods.getDetails() + "<br/>" + goods.getPrompt() + "<br/> 可使用门店：<br/>");
        for(Shop shop : goods.shops){
            desc.append(shop.name + "," + shop.address + "<br/>");
        }
        BigDecimal price = goods.getResalePrice(resaler.level);
        if(pricemakupRate != null){
            price = price.multiply(new BigDecimal(pricemakupRate).add(BigDecimal.ONE));
        }else if(pricemakup != null){
            price = price.add(new BigDecimal(pricemakup));
        }
        System.out.print("####price :" + price);
        ItemAddResponse response = addItem(taobaoClient, token.accessToken, goods.baseSale, 
                price.setScale(2,BigDecimal.ROUND_UP).toString(), goods.name, goods.getDetails());
        
        if(response != null && response.getErrorCode() == null){
            //ignore upload image error
            uploadImg(taobaoClient, token.accessToken,response.getItem().getNumIid(), new File(IMG_ROOT_PATH, goods.imagePath));
        }
        return response;
    }
    
    /**
     * 上传商品到淘宝——仅基本信息部分
     * 
     * @param taobaoClient  淘宝api客户端对象
     * @param token         OAuth token
     * @param num           商品数量
     * @param price         商品价格
     * @param title         商品标题
     * @param desc          商品描述
     * @return              上传结果
     */
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
    
    /**
     * 上传商品图片到淘宝的某一商品，并设置为主图片
     * 
     * @param taobaoClient  淘宝api客户端对象
     * @param token         OAuth token
     * @param numIid        淘宝商品ID
     * @param imgFile       图片文件
     * @return              上传图片结果
     */
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
