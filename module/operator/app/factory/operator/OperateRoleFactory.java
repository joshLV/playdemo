package factory.operator;

import factory.FactoryBoy;
import factory.ModelFactory;
import factory.callback.BuildCallback;
import models.operator.OperateRole;

import java.util.Date;
import java.util.HashSet;

/**
 * User: wangjia
 * Date: 12-11-19
 * Time: 下午1:28
 */
public class OperateRoleFactory extends ModelFactory<OperateRole> {
    @Override
    public OperateRole define() {
        OperateRole role = new OperateRole();
        role.loadVersion = 1331545508967l;
        role.lockVersion = 0;
        role.createdAt = new Date();
        role.updatedAt = new Date();
        role.permissions = new HashSet<>();
        return role;
    }

    public static void createRoles(String... roleKeys) {
        OperateRole role;
        for (final String value : roleKeys) {
            FactoryBoy.create(OperateRole.class,
                    new BuildCallback<OperateRole>() {
                        @Override
                        public void build(OperateRole role) {
                            role.text = value;
                            role.key = value;
                        }
                    });
        }

    }


}
