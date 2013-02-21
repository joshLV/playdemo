package factory.operator;

import factory.ModelFactory;
import models.operator.OperateNavigation;

import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/21/12
 * Time: 11:06 AM
 */
public class OperateNavigationFactory extends ModelFactory<OperateNavigation> {
    @Override
    public OperateNavigation define() {
        OperateNavigation navigation = new OperateNavigation();
        navigation.name = "main";
        navigation.text="系统管理";
        navigation.url="http://www.google.com";
        navigation.action = "systemManager";
        navigation.actived = true;
        navigation.applicationName = "operate-admin";
        navigation.createdAt = new Date();
        navigation.description = "";
        navigation.devBaseUrl = "http://localhost:9107/";
        navigation.displayOrder = 100;
        navigation.labels = "";
        return navigation;
    }
}