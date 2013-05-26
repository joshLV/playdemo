package controllers;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.PictureUploadRequest;
import com.taobao.api.response.PictureUploadResponse;
import models.accounts.AccountType;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.resale.Resaler;
import models.taobao.TaobaoCouponUtil;
import play.mvc.Controller;

import java.io.File;

/**
 * @author likang
 *         Date: 13-5-26
 */
public class KtvTaobaoImgUpload extends Controller {
    public static void upload(Long resalerId, File imgFile) {
        Resaler resaler = Resaler.findById(resalerId);
        if (resaler == null || imgFile == null) {
            renderJSON("{\"error\":1,\"message\":\"参数错误01\"}");
            return;
        }
        PictureUploadRequest request = new PictureUploadRequest();
        request.setPictureCategoryId(0L);
        request.setImg(new FileItem(imgFile));
        request.setImageInputTitle("seewi_img_");


        TaobaoClient taobaoClient = new DefaultTaobaoClient(
                TaobaoCouponUtil.URL, resaler.taobaoCouponAppKey, resaler.taobaoCouponAppSecretKey);
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);
        PictureUploadResponse response;
        try {
            response = taobaoClient.execute(request, token.accessToken);
        } catch (ApiException e) {
            renderJSON("{\"error\":1,\"message\":\"" + e.getErrMsg() + "\"}");
            return;
        }
        String url = response.getPicture().getPicturePath();
        if (url.startsWith("http://img.taobaocdn.com/imgextra/http://")) {
            url = url.substring(34);
        }
        renderJSON("{\"error\":0,\"url\":\"" + url + "\"}");
    }
}
