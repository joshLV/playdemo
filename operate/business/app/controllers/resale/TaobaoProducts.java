package controllers.resale;

import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.FileItem;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.ItemAddRequest;
import com.taobao.api.request.ItemImgUploadRequest;
import com.taobao.api.response.ItemAddResponse;
import com.taobao.api.response.ItemImgUploadResponse;
import controllers.OperateRbac;
import models.accounts.AccountType;
import models.admin.OperateUser;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.resale.Resaler;
import models.sales.Goods;
import models.sales.ResalerProduct;
import models.sales.ResalerProductStatus;
import operate.rbac.annotations.ActiveNavigation;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.math.BigDecimal;

/**
 * @author likang
 *         Date: 13-1-29
 */
@With(OperateRbac.class)
@ActiveNavigation("resale_partner_product")
public class TaobaoProducts extends Controller{
    private static final String URL = Play.configuration.getProperty("taobao.top.url", "http://gw.api.taobao.com/router/rest");
    private static final String APPKEY = Play.configuration.getProperty("taobao.top.appkey", "21293912");
    private static final String APPSECRET = Play.configuration.getProperty("taobao.top.appsecret", "1781d22a1f06c4f25f1f679ae0633400");

    public static String IMG_ROOT_PATH = Play.configuration.getProperty("upload.imagepath", "");

    @ActiveNavigation("resale_partner_product")
    public static void showUpload(Long goodsId) {
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }
        render(goods);
    }
    @ActiveNavigation("resale_partner_product")
    public static void upload(Long num, Long goodsId, BigDecimal price, BigDecimal faceValue, String type,
                              String stuffStatus,String title, String desc, String locationState,
                              String locationCity, Long cid, String props, String approveStatus,
                              String startDate, String endDate, String[] sellerCids) {
        OperateUser operateUser = OperateRbac.currentUser();
        Goods goods = Goods.findById(goodsId);
        if (goods == null) {
            notFound();
        }

        ResalerProduct product = ResalerProduct.alloc(OuterOrderPartner.TB, goods);

        ItemAddRequest addRequest = new ItemAddRequest();
        addRequest.setNum(num > 999999 ? 999999 : num);// 商品数量
        addRequest.setPrice(price.setScale(2, BigDecimal.ROUND_UP).toString());
        addRequest.setType(type);
        addRequest.setStuffStatus(stuffStatus);
        addRequest.setTitle(title);
        addRequest.setDesc(desc);
        addRequest.setLocationState(locationState);
        addRequest.setLocationCity(locationCity);
        addRequest.setCid(cid);//分类类别：餐饮
        //类别：品牌：城市
        addRequest.setProps(props);
        addRequest.setInputStr(faceValue.toString()); //面值value
        addRequest.setInputPids("5392163");//面值key
        addRequest.setApproveStatus(approveStatus);//初始为下架的，在淘宝仓库中
        addRequest.setOuterId(String.valueOf(product.goodsLinkId));
        addRequest.setLocalityLifeExpirydate(startDate+ "," + endDate);
        addRequest.setSellerCids(StringUtils.join(sellerCids));

        TaobaoClient taobaoClient = new DefaultTaobaoClient(URL, APPKEY, APPSECRET);

        //找到淘宝的token
        Resaler resaler = Resaler.findOneByLoginName(Resaler.TAOBAO_LOGIN_NAME);
        OAuthToken token = OAuthToken.getOAuthToken(resaler.id, AccountType.RESALER, WebSite.TAOBAO);


        try {
            ItemAddResponse addResponse = taobaoClient.execute(addRequest, token.accessToken);
            if (addResponse != null && addResponse.getErrorCode() == null) {
                renderArgs.put("addResponse", addResponse);
                Long taobaoItemId = addResponse.getItem().getNumIid();

                //保存商品状态
                product.creator(operateUser.id).partnerProduct(taobaoItemId)
                        .status(ResalerProductStatus.UPLOADED).save();

                ItemImgUploadRequest imgUploadRequest = new ItemImgUploadRequest();
                imgUploadRequest.setNumIid(taobaoItemId);
                imgUploadRequest.setImage(new FileItem(new File(IMG_ROOT_PATH, goods.imagePath)));
                imgUploadRequest.setIsMajor(true);

                ItemImgUploadResponse imgUploadResponse = taobaoClient.execute(imgUploadRequest, token.accessToken);
                renderArgs.put("imgUploadResponse", imgUploadResponse);

            }
        } catch (ApiException e) {
            Logger.info(e, "add item to taobao failed");
        } catch (Exception e) {
            Logger.info(e, "add item to taobao failed");
        }
        render("resale/TaobaoProducts/result.html");
    }
}

