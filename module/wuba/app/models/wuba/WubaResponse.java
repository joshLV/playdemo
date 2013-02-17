package models.wuba;

import com.google.gson.JsonElement;

/**
 * @author likang
 *         Date: 13-1-23
 */
public class WubaResponse {
    public String status = "10000";
    public String msg;
    public String code;
    public JsonElement data;

    public boolean isOk() {
        return "10000".equals(status);
    }

    @Override
    public String toString() {
        return "code: " + code + "\nstatus: " + status + "\nmsg: " + msg
                + "\ndata:\n"  + (data == null ? "null" : data.toString());
    }
}
