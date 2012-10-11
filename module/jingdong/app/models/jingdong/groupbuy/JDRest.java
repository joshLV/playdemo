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
public class JDRest<T extends JDMessage> {
    public String  version;
    public Long    venderId;
    public String  venderKey;
    public Boolean zip;
    public Boolean encrypt;
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
        version = root.elementTextTrim("Version");
        venderId = Long.parseLong(root.elementTextTrim("VenderId"));
        venderKey = root.elementTextTrim("VenderKey");
        zip = Boolean.parseBoolean(root.elementTextTrim("Zip"));
        encrypt = Boolean.parseBoolean(root.elementTextTrim("Encrypt"));

        Element messageElement = null;
        if(encrypt){
            //解析加密字符串
            String rawMessage = root.elementTextTrim("Data");
            String encryptedMessage = JDGroupBuyUtil.decryptMessage(rawMessage);
            Document encryptedMessageDocument = null;
            try{
                encryptedMessageDocument = DocumentHelper.parseText(encryptedMessage);
            }catch (DocumentException e){
                Logger.info("failed to parse encrypted message of JingDong request");
                return false;
            }
            messageElement = encryptedMessageDocument.getRootElement();
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
