package unit.models.sms;

import models.sms.SMSMessage;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

/**
 * User: likang
 */
public class SMSMessageTest extends UnitTest{
    
    @Test
    public void TestSMSMessage(){
        String content = "test content";
        String phoneNumber = "123456";
        SMSMessage message = new SMSMessage(content, phoneNumber, "0000");
        
        assertEquals(content + "【一百券】", message.getContent());
        assertEquals(1, message.getPhoneNumbers().size());
        assertEquals(phoneNumber, message.getPhoneNumbers().get(0));
        
        String newContent = "new content";
        String newPhoneNumber = "654321";
        List<String> newPhones = new ArrayList<>();
        newPhones.add(newPhoneNumber);

        message.setContent(newContent);
        message.setPhoneNumbers(newPhones);

        assertEquals(newContent + "【一百券】", message.getContent());
        assertEquals(1, message.getPhoneNumbers().size());
        assertEquals(newPhoneNumber, message.getPhoneNumbers().get(0));
        
        String toString = "SMSMessage [content=" + newContent + "【一百券】,code=0000,phones="+newPhoneNumber+",]";
        assertEquals(toString, message.toString());
    }
    
}
