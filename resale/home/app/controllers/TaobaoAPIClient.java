package controllers;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.request.ItemImgUploadRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemImgUploadResponse;
import controllers.modules.resale.cas.SecureCAS;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.resale.Resaler;
import models.resale.ResalerFav;
import models.sales.Goods;
import models.sales.Shop;
import play.Logger;
import play.Play;
import play.data.binding.As;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

@With(SecureCAS.class)
public class TaobaoAPIClient extends Controller {
    private static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    private static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    private static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");

    public static String IMG_ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");
    public static String TAOBAO_TOP_OAUTH_URL = Play.configuration.getProperty("taobao.top.oauth_url", "http://container.api.taobao.com/container?appkey=21293912&encode=utf-8");

    public static void add(Long goodsId) {
        Resaler user = SecureCAS.getResaler();
        OAuthToken token = OAuthToken.getOAuthToken(user.getId(), AccountType.RESALER, WebSite.TAOBAO);
        ResalerFav resalerFav = ResalerFav.findByGoodsId(user,goodsId);
        if (resalerFav == null) {
            error("no fav found");
        }
        if (token == null || token.isExpired()) {
            redirect(TAOBAO_TOP_OAUTH_URL);
        }

        ItemAddResponse response = uploadGoods(resalerFav.goods, token, user, null, null);
        if (response == null) {
            renderArgs.put("errMsg", "");
            render("ResalerFavs/topError.html");
        } else if (response.getErrorCode() != null) {
            renderArgs.put("errMsg", "请求淘宝服务出现异常：ErrorCode:" + response.getErrorCode() +
                    "; ErrorMessage:" + response.getMsg() +
                    "; SubErrorCode:" + response.getSubCode() +
                    "; SubErrorMessage:" + response.getSubMsg());
            render("ResalerFavs/topError.html");
        } else {
            Long iid = response.getItem().getNumIid();
            resalerFav.taobaoItemId = iid;
            resalerFav.save();
            redirect("/library");
        }
    }

    /**
     * 批量发布商品到淘宝
     *
     * @param goodsIds       商品ID列表
     * @param pricemakupRate 提价百分比
     * @param pricemakup     提价额
     */
    public static void batchAdd(@As(",") List<Long> goodsIds, int pricemakupRate, String pricemakup) {
        Resaler user = SecureCAS.getResaler();
        OAuthToken token = OAuthToken.getOAuthToken(user.getId(), AccountType.RESALER, WebSite.TAOBAO);
        if (token == null || token.isExpired()) {
            redirect(TAOBAO_TOP_OAUTH_URL);
        }
        for (Long goodsId : goodsIds) {
            Goods goods = Goods.findById(goodsId);
            ResalerFav resalerFav = ResalerFav.find("byGoods", goods).first();
            if (resalerFav == null) {
                continue;
            }
            ItemAddResponse response = uploadGoods(resalerFav.goods, token, user, String.valueOf(pricemakupRate / 100.0), pricemakup);
            if (response != null && response.getErrorCode() == null) {
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
     * @param goods   商品
     * @param token   OAuth token
     * @param resaler 分销商
     * @return 上传结果
     */
    private static ItemAddResponse uploadGoods(Goods goods, OAuthToken token, Resaler resaler, String pricemakupRate, String pricemakup) {
        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);

        StringBuilder desc = new StringBuilder(goods.getDetails() + "<br/>" + goods.getPrompt() + "<br/> 可使用门店：<br/>");
        for (Shop shop : goods.shops) {
            desc.append(shop.name + "," + shop.address + "<br/>");
        }
        BigDecimal price = goods.getResalePrice();
        if (pricemakupRate != null) {
            price = price.multiply(new BigDecimal(pricemakupRate).add(BigDecimal.ONE));
        } else if (pricemakup != null) {
            price = price.add(new BigDecimal(pricemakup));
        }

        ItemAddResponse response = addItem(taobaoClient, goods, token.accessToken, price.setScale(2, BigDecimal.ROUND_UP).toString());
        if (response != null && response.getErrorCode() == null) {
            //ignore upload image error
            uploadImg(taobaoClient, token.accessToken, response.getItem().getNumIid(), new File(IMG_ROOT_PATH, goods.imagePath));
        }
        return response;
    }


    private static String getDetails(Goods goods) {
        StringBuilder builder = new StringBuilder();
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">商品详情</span>");
        builder.append("</p>" + goods.getExhibition());
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">温馨提示</span>");
        builder.append("</p>" + goods.getPrompt());
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">小贴士</span>");
        builder.append("</p>");
        builder.append("<p>·本商品为电子券，购买后会以短信形式发送一串数字到您的手机</p>");
        builder.append("<p>·如果您不小心遗失了您的电子券，请拨打一百券客服热线</p> ");
        builder.append("<p>·发票由最终服务提供商开具 用户跟商家索取发票</p>  ");
        builder.append("<p>·本产品由【一百券】提供，客服热线：400-6262-166 </p> ");
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">商品展示</span>");
        builder.append("</p>" + goods.getDetails());
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">商户介绍</span>");
        builder.append("</p>" + goods.getSupplierDes());
        builder.append("<p style=\"BACKGROUND-COLOR: rgb(204,0,0); FONT-WEIGHT: bold\">");
        builder.append("<span style=\"font-family:SimSun;font-size:small;color:#ffffff;\">适用门店</span>");
        builder.append("</p>");
        builder.append("<table border=\"1\" cellpadding=\"4\" cellspacing=\"0\">");
        for (Shop shop : goods.getShopList()) {
            builder.append("<tr>");
            builder.append("<td width=\"150px\">" + shop.name + "</td>");
            builder.append("<td width=\"380px\">地址：" + shop.address + " <br> 电话：" + shop.phone + "</td>");
            builder.append("<td width=\"200px\">交通：" + shop.transport + "</td>");
            builder.append("</tr>");
        }
        builder.append("</table> ");
        return builder.toString();
    }

    /**
     * 上传商品到淘宝——仅基本信息部分
     *
     * @param taobaoClient 淘宝api客户端对象
     * @param goods        商品
     * @param token        OAuth token
     * @param price        商品价格
     * @return 上传结果
     */
    private static ItemAddResponse addItem(TaobaoClient taobaoClient, Goods goods, String token, String price) {
        ItemAddRequest addRequest = new ItemAddRequest();
        Long num = goods.getRealStocks();
        addRequest.setNum(num > 999999 ? 999999 : num);// 商品数量
        addRequest.setPrice(price);
        addRequest.setType("fixed");
        addRequest.setStuffStatus("new");
        addRequest.setTitle(goods.title);
        addRequest.setDesc(getDetails(goods));
        addRequest.setLocationState("上海");
        addRequest.setLocationCity("上海");
        addRequest.setCid(50015759L);//分类类别：餐饮
        //类别：品牌：城市
        addRequest.setProps("2001943:3262426;3816036:3871548;8648185:29423;8648373:29423;");
        addRequest.setInputStr(String.valueOf(goods.faceValue.setScale(0))); //面值value
        addRequest.setInputPids("5392163");//面值key
        addRequest.setApproveStatus("instock");//初始为下架的，在淘宝仓库中
        addRequest.setOuterId(goods.id.toString());

        ItemAddResponse response;
        try {
            response = taobaoClient.execute(addRequest, token); //执行API请求并打印结果
            Logger.debug("ItemAddResponse.body:" + response.getBody());
            return response;
        } catch (ApiException e) {
            Logger.error(e, "add item to taobao failed");
            return null;
        }

    }

    /**
     * 上传商品图片到淘宝的某一商品，并设置为主图片
     *
     * @param taobaoClient 淘宝api客户端对象
     * @param token        OAuth token
     * @param numIid       淘宝商品ID
     * @param imgFile      图片文件
     * @return 上传图片结果
     */
    private static ItemImgUploadResponse uploadImg(TaobaoClient taobaoClient, String token, Long numIid, File imgFile) {
        ItemImgUploadRequest imgUploadRequest = new ItemImgUploadRequest();
        imgUploadRequest.setNumIid(numIid);
        imgUploadRequest.setImage(new FileItem(imgFile));
        imgUploadRequest.setIsMajor(true);

        try {
            ItemImgUploadResponse response = taobaoClient.execute(imgUploadRequest, token); //执行API请求并打印结果
            Logger.debug("ItemImgUploadResponse.body:" + response.getBody());
            return response;
        } catch (ApiException e) {
            Logger.error(e, "upload img to taobao failed");
            return null;
        }

    }
}
