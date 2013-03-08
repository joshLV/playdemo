package factory;

import models.WebSqlCommand;
import models.operator.OperateUser;
import util.DateHelper;

/**
 * User: tanglq
 * Date: 13-3-8
 * Time: 下午2:22
 */
public class WebSqlCommandFactory extends ModelFactory<WebSqlCommand> {
    @Override
    public WebSqlCommand define() {
        WebSqlCommand command = new WebSqlCommand();
        command.sql = "select * from users";
        command.operateUser = FactoryBoy.lastOrCreate(OperateUser.class);
        command.executedAt = DateHelper.beforeMinuts(10);
        command.referCount = 1;
        command.resultCount = 30;
        command.sqlResult = "{}";
        return command;
    }
}
