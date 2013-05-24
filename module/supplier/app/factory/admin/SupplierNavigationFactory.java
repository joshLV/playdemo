package factory.admin;

import factory.ModelFactory;
import models.admin.SupplierNavigation;

import java.util.Date;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/16/12
 * Time: 9:36 AM
 */
public class SupplierNavigationFactory extends ModelFactory<SupplierNavigation> {

    @Override
    public SupplierNavigation define() {
        SupplierNavigation navigation = new SupplierNavigation();
        navigation.name = "main";
        navigation.text="系统管理";
        navigation.url="http://www.google.com";
        navigation.action = "systemManager";
        navigation.actived = true;
        navigation.applicationName = "traders-home";
        navigation.createdAt = new Date();
        navigation.description = "";
        navigation.devBaseUrl = "http://localhost:9101/";
        navigation.displayOrder = 100;
        navigation.labels = "";
        return navigation;
    }
}
