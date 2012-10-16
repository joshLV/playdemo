package models.jingdong.groupbuy;

import models.jingdong.JDGroupBuyUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import play.Logger;

/**
 * @author likang
 * Date: 12-9-28
 */
public class JDResponse<T extends JDMessage> {
    public String  version;
    public Long    venderId;
    public Boolean zip;
    public Boolean encrypt;
    public String resultCode;
    public String resultMessage;
    public T       data;

    public boolean parse(String xml, T d){
        Logger.info("jingdong request:" + xml);
        Document document = null;
        try{
            document = DocumentHelper.parseText(xml);
        }catch (DocumentException e){
            Logger.info("failed to parse JingDong request");
            return false;
        }

        Element root = document.getRootElement();
        resultCode = root.elementTextTrim("ResultCode");
        if (!"200".equals(resultCode)){
            return false;
        }
        version = root.elementTextTrim("Version");
        venderId = Long.parseLong(root.elementTextTrim("VenderId"));
        zip = Boolean.parseBoolean(root.elementTextTrim("Zip"));
        encrypt = Boolean.parseBoolean(root.elementTextTrim("Encrypt"));
        resultMessage = root.elementTextTrim("ResultMessage");

        Element messageElement = null;
        if(encrypt){
            //解析加密字符串
            String rawMessage = root.elementTextTrim("Data");
            String decryptedMessage = JDGroupBuyUtil.decryptMessage(rawMessage);
            Document messageDocument = null;
            try{
                messageDocument = DocumentHelper.parseText(decryptedMessage);
            }catch (DocumentException e){
                Logger.info("failed to parse encrypted message of JingDong request");
                return false;
            }
            messageElement = messageDocument.getRootElement();
        } else{
            Element dataElement = root.element("Data");
            if(dataElement != null){
                messageElement = dataElement.element("Message");
            }
        }
        data = d;
        return messageElement != null && data.parse(messageElement);
    }
}
