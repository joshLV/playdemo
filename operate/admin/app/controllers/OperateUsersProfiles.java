package controllers;

import models.operator.OperateRole;
import models.operator.OperateUser;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * TODO.
 * <p/>
 * User: yanjy
 * Date: 12-6-1
 * Time: 上午10:15
 */

@With(OperateRbac.class)
public class OperateUsersProfiles extends Controller {

    public static void index() {
        Long id = OperateRbac.currentUser().id;
        OperateUser operateUser = OperateUser.findById(id);
        String roleIds = "";
        if (operateUser.roles != null && !operateUser.roles.isEmpty()) {
            for (OperateRole role : operateUser.roles) {
                roleIds += role.id + ",";
            }
        }
        render(operateUser, roleIds);
    }

    /**
     * 操作员信息修改
     *
     * @param operateUser 用户信息
     */
    public static void update(@Valid OperateUser operateUser) {
        if (Validation.hasErrors()) {
            List rolesList = OperateRole.findAll();
            String roleIds = "";
            if (!operateUser.roles.isEmpty()) {
                for (OperateRole role : operateUser.roles) {
                    roleIds += role.id + ",";
                }
            }
            render("OperateUsersProfiles/index.html", operateUser, roleIds);
        }
        Long id = OperateRbac.currentUser().id;
        // 更新用户信息
        OperateUser.update(id, operateUser);

        index();
    }

}
