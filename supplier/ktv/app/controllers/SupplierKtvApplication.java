package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import models.accounts.AccountType;
import models.ktv.*;
import models.oauth.OAuthToken;
import models.oauth.WebSite;
import models.order.OuterOrderPartner;
import models.sales.ResalerProduct;
import org.apache.commons.lang.StringUtils;
import play.*;
import play.libs.Crypto;
import play.mvc.*;

import java.util.*;

import models.*;

public class SupplierKtvApplication extends Controller {

    public static void index() {
        render();
    }
}