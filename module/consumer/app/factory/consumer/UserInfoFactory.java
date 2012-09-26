package factory.consumer;

import factory.ModelFactory;
import models.consumer.UserInfo;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: wangjia
 * Date: 12-9-26
 * Time: 下午2:16
 * To change this template use File | Settings | File Templates.
 */
public class UserInfoFactory extends ModelFactory<UserInfo> {

    @Override
    public UserInfo define() {
        UserInfo userInfo = new UserInfo();
        userInfo.fullName="";
        userInfo.birthday="2001-01-01";
        userInfo.phone="";
        userInfo.marryState=0;
        userInfo.position="0";
        userInfo.userqq="";
        userInfo.salary=2500;
        userInfo.interest="";
        userInfo.industry="";
        userInfo.otherInfo="";
        userInfo.createdAt=new Date();
        userInfo.totalPoints=(long)100;

        return userInfo;
    }

}
