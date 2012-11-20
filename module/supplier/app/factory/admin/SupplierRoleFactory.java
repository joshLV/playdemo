package factory.admin;

import factory.ModelFactory;
import models.admin.SupplierRole;

import java.util.Date;
import java.util.HashSet;

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
        role.loadVersion = 1331545508967l;
        role.lockVersion = 0;
        role.createdAt = new Date();
        role.updatedAt = new Date();
        role.permissions = new HashSet<>();
        return role;
    }
}
