package factory.consumer;

import models.consumer.UserWebIdentification;
import util.DateHelper;
import factory.FactoryBoy;
import factory.ModelFactory;

public class UserWebIdentificationFactory extends ModelFactory<UserWebIdentification> {

    @Override
    public UserWebIdentification define() {
        UserWebIdentification uwi = new UserWebIdentification();
        uwi.firstPage = "http://www.yibaiquan.com";
        uwi.referer = "http://www.baidu.com/serach?q=yibaiquan";
        uwi.referCode = "code312";
        uwi.cookieId = "32134-87071-87813412-sa8134010" + FactoryBoy.sequence(UserWebIdentification.class);
        uwi.createdAt = DateHelper.beforeMinuts(1);
        uwi.refererHost = "www.baidu.com";
        uwi.userAgent = "IE6";
        return uwi;
    }

}
