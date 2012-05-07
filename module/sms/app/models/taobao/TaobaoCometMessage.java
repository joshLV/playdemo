package models.taobao;

import java.io.Serializable;

/**
 * @author : likang
 * Date: 12-5-3
 */
public class TaobaoCometMessage implements Serializable {
    String message;

    public TaobaoCometMessage(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){
        return message;
    }

}
