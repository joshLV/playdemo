package models.sina;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * User: yan
 * Date: 13-3-21
 * Time: 上午10:14
 */
public class SinaVoucherResponse {
    public JsonObject header = null;

    public JsonElement content = null;

    public JsonObject error = null;


    public boolean isOk() {
        return error == null;
    }
}
