package models.baidu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * User: yan
 * Date: 13-7-11
 * Time: 下午4:24
 */
public class BaiduResponse {
    public String msg;
    public String code;
    public JsonObject res;
    public JsonElement data;

    public boolean isOk() {
        if (code != null) {
            return "0".equals(code);
        }
        return res != null;
    }

    @Override
    public String toString() {
        return "code: " + code + "\nmsg: " + msg
                + "\nres:\n" + (res == null ? "null" : res.toString());
    }
}
