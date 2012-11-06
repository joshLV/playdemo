package models.jingdong.groupbuy;

import org.apache.commons.lang.StringUtils;
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
    public String   version;
    public Long     venderId;
    public Boolean  zip;
    public Boolean  encrypt;
    public String   resultCode;
    public String   resultMessage;
    public T        data;

    public boolean parse(String xml, T d){
        data = d;
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
        zip = Boolean.parseBoolean(root.elementTextTrim("Zip"));
        encrypt = Boolean.parseBoolean(root.elementTextTrim("Encrypt"));

        // 只有作为京东的响应的时候， resultCode 和 resultMessage 才有用
        resultCode = root.elementTextTrim("ResultCode");
        resultMessage = root.elementTextTrim("ResultMessage");
        if(StringUtils.isNotEmpty(resultCode) && !"200".equals(resultCode)){
            Logger.info("resultCode is not 200");
            return false;
        }

        Element messageElement = null;
        if(encrypt){
            String rawMessage = root.elementTextTrim("Data");
            //解析加密字符串
            String decryptedMessage = JDGroupBuyUtil.decryptMessage(rawMessage);
            Logger.info("decryptedMessage, %s", decryptedMessage);
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
        return messageElement != null && data.parse(messageElement);
    }
}
