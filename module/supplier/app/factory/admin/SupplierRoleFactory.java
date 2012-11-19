package factory.admin;

import factory.ModelFactory;
import models.admin.SupplierRole;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 11/19/12
 * Time: 1:49 PM
 */
public class SupplierRoleFactory extends ModelFactory<SupplierRole> {

    @Override
    public SupplierRole define() {
        SupplierRole role = new SupplierRole();
        role.key = "admin";
        role.text = "";
        role.description = "";
        return role;
    }
}
